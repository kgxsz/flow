(ns flow.middleware
  (:require [flow.entity.user :as user]
            [flow.entity.authorisation :as authorisation]
            [flow.entity.utils :as entity.u]
            [flow.query :as query]
            [flow.access-control :as access-control]
            [flow.utils :as u]
            [medley.core :as medley]
            [ring.middleware.cors :as cors.middleware]
            [ring.middleware.session :as session.middleware]
            [ring.middleware.session.cookie :as cookie]
            [muuntaja.middleware :as muuntaja.middleware]
            [slingshot.slingshot :as slingshot]
            [clojure.spec.alpha :as s]))


(defn wrap-access-control
  "For inbound requests, ensures that only the queries and commands that correspond to the appropriate
   level of access are passed through. For outbound responses, works through each entity in the response's
   body and selects the keys that correspond to the correct level of access:
   - The default accessible keys, which are visible to anybody.
   - The owner accessible keys, which are visible when the current user owns the entity.
   - The role accessible keys, which are visible when the current user has the corresponding role.
   If an entity has had all its keys removed then the entity itself will be removed from the response."
  [handler]
  (fn [request]
    (let [current-user (get-in request [:body-params :session :current-user])
          response (-> request
                       (update-in [:body-params :query]
                                  access-control/select-accessible-queries
                                  current-user)
                       (update-in [:body-params :command]
                                  access-control/select-accessible-commands
                                  current-user)
                       (handler))
          ;; NOTE - some of the current user's fields may be stale during the
          ;; outbound portion of this middleware. This is okay though since the
          ;;fields used to determine access control aren't mutable by commands.
          current-user (get-in response [:body :session :current-user])]
      (-> response
          (update-in [:body :users]
                     (partial medley/map-vals
                              #(access-control/select-accessible-user-keys % current-user)))
          (update-in [:body :users] (partial medley/remove-vals empty?))
          (update-in [:body :authorisations]
                     (partial medley/map-vals
                              #(access-control/select-accessible-authorisation-keys % current-user)))
          (update-in [:body :authorisations] (partial medley/remove-vals empty?))))))


(defn wrap-current-user
  "For inbound requests, takes the current user id found in the session and fetches
   the current user, then attaches it to the session. If the current user cannot be
   fetched, then an exception is raised. For outbound responses, updates the current
   user id in the session while removing the current user itself."
  [handler]
  (fn [request]
    (let [current-user-id (get-in request [:body-params :session :current-user-id])
          current-user (when (uuid? current-user-id)
                         (u/validate :db/user (user/fetch current-user-id)))
          request (-> request
                      (update-in [:body-params :session] dissoc :current-user-id)
                      (assoc-in [:body-params :session :current-user] current-user))
          {:keys [body] :as response} (handler request)]
      (let [id (get-in response [:body :session :current-user :user/id])]
        (-> response
            (assoc-in [:body :session :current-user-id] id)
            (update-in [:body :session] dissoc :current-user))))))


(defn wrap-metadata
  "For the inbound requests does nothing, for outbound responses, filters
   the metadata such that only the appropriate fields are returned."
  [handler]
  (fn [request]
    (let [response (handler request)
          {:keys [users authorisations] :as metadata} (get-in response [:body :metadata])]
      (cond-> response
         metadata (update-in [:body :metadata] select-keys [:id-resolution :users :authorisations])
         users (update-in [:body :metadata :users] select-keys [:next-offset :exhausted?])
         authorisations (update-in [:body :metadata :authorisations] select-keys [:next-offset :exhausted?])))))


(defn wrap-session
  "For the inbound requests, takes the persisted session and puts it into the body params
   for use directly in the query/command. For the outbound response, ensures that the
   session is present in the body is placed in the request so as to be persisted."
  [handler]
  (fn [{:keys [session] :as request}]
    ;; NOTE - today the client can send over session information as part of the body params
    ;; but we ignore this completely as it's untrustworthy and we prefer to rely on the session
    ;; information encrypted in the tamper proof cookie. Eventually though, we'll want to allow
    ;; the client to suggest session updates that we can then carefully merge safely in here.
    (let [request (assoc-in request [:body-params :session] session)
          response (handler request)
          session (get-in response [:body :session])]
      (if session
        (assoc response :session session)
        (u/generate :internal-error "Session missing from response body.")))))


(defn wrap-session-persistence
  "Ensures that sessions are persisted in an encrypted cookie on the client."
  [handler]
  (session.middleware/wrap-session
   handler
   {:cookie-name "session"
    :cookie-attrs {:max-age 604800 ;; one week
                   :domain (System/getenv "COOKIE_ATTRIBUTE_DOMAIN")
                   :path "/"
                   :http-only true
                   :same-site :none
                   :secure true}
    :store (cookie/cookie-store {:key (System/getenv "COOKIE_STORE_KEY")})}))


(defn wrap-content-validation
  "Determines the validity of the content for both inbound request and outbound responses."
  [handler]
  (fn [request]
    (if (s/valid? :request/body-params (:body-params request))
      (update (handler request) :body (partial u/validate :response/body))
      (u/generate :unsupported-request "Invalid request content."))))


(defn wrap-content-type
  "Formats the inbound request and outbound response based on the content type header."
  [handler]
  (fn [request]
    (let [content-type (get-in request [:headers "content-type"])]
      (if (= content-type "application/transit+json")
        (slingshot/try+
          ((muuntaja.middleware/wrap-format handler) request)
          (catch [:type :muuntaja/decode] {:keys [format]}
            (u/generate :unsupported-request (str "Malformed " format " content."))))
        (u/generate :unsupported-request "Unsupported or missing Content-Type header.")))))


(defn wrap-request-path
  "Filters out any request path other than the root."
  [handler]
  (fn [{:keys [uri] :as request}]
    (if (or (= uri "/") (= uri ""))
      (handler request)
      (u/generate :unsupported-request "Unsupported request path."))))


(defn wrap-request-method
  "Filters out any request method other than POST."
  [handler]
  (fn [{:keys [request-method] :as request}]
    (if (= request-method :post)
      (handler request)
      (u/generate :unsupported-request "Unsupported request method."))))


(defn wrap-exception
  "Handles all uncaught exceptions."
  [handler]
  (fn [request]
    (slingshot/try+
     (handler request)
      (catch [:type :flow/unsupported-request] {:keys [message]}
        {:status 400
         :headers {"Content-Type" "application/json; charset=utf-8"}
         :body (str "{\"error\": \"" message "\"}")})
      (catch [:type :flow/internal-error] {:keys [message]}
        {:status 500
         :headers {"Content-Type" "application/json; charset=utf-8"}
         :body (str "{\"error\": \"" message "\"}")})
      (catch [:type :flow/external-error] _
        {:status 500
         :headers {"Content-Type" "application/json; charset=utf-8"}
         :body "{\"error\": \"External error detected.\"}"})
      (catch Object _
        {:status 500
         :headers {"Content-Type" "application/json; charset=utf-8"}
         :body "{\"error\": \"Unspecified error detected.\"}"}))))


(defn wrap-cors
  "Handles the cross origin resource sharing concerns."
  [handler]
  (cors.middleware/wrap-cors
   handler
   :access-control-allow-origin [(re-pattern (System/getenv "CORS_ORIGIN"))]
   :access-control-allow-methods [:options :post]
   :access-control-allow-headers ["Content-Type"]
   :access-control-allow-credentials "true"))
