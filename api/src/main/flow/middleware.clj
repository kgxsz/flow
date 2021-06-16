(ns flow.middleware
  (:require [flow.entity.user :as user]
            [flow.entity.authorisation :as authorisation]
            [flow.entity.utils :as entity.u]
            [flow.utils :as u]
            [medley.core :as medley]
            [ring.middleware.cors :as cors.middleware]
            [ring.middleware.session :as session.middleware]
            [ring.middleware.session.cookie :as cookie]
            [muuntaja.middleware :as muuntaja.middleware]
            [clojure.spec.alpha :as s]))


(defn wrap-access-control
  "For inbound requests does nothing. For outbound responses, works through each entity
   in the response's body and selects the keys that correspond to the correct level of access:
   - The default accessible keys, which are visible to anybody.
   - The owner accessible keys, which are visible when the current user owns the entity.
   - The role accessible keys, which are visible when the current user has the corresponding role.
   If an entity has had all its keys removed then the entity itself will be removed from the response."
  [handler]
  (fn [request]
    (let [response (handler request)
          current-user (get-in response [:body :metadata :current-user])]
      (-> response
          (update-in [:body :users]
           #(medley/deep-merge
             (medley/map-vals user/select-default-accessible-keys %)
             (medley/map-vals (partial user/select-owner-accessible-keys current-user) %)
             (medley/map-vals (partial user/select-role-accessible-keys current-user) %)))
          (update-in [:body :users] (partial medley/remove-vals empty?))
          (update-in [:body :authorisations]
           #(medley/deep-merge
             (medley/map-vals authorisation/select-default-accessible-keys %)
             (medley/map-vals (partial authorisation/select-owner-accessible-keys current-user) %)
             (medley/map-vals (partial authorisation/select-role-accessible-keys current-user) %)))
          (update-in [:body :authorisations] (partial medley/remove-vals empty?))))))


(defn wrap-current-user
  "For inbound requests, takes the current user id found in the session and fetches
   the current user, then attaches it to the session. For outbound responses, updates
   the current user id in the session while removing the current user itself."
  [handler]
  (fn [request]
    (let [id (get-in request [:body-params :session :current-user-id])
          request (assoc-in request [:body-params :session :current-user] (user/fetch id))
          {:keys [body] :as response} (handler request)]
      (if-let [id (get-in response [:body :session :current-user :user/id])]
        (-> response
            (assoc-in [:body :session :current-user-id] id)
            (update-in [:body :session] dissoc :current-user))
        (-> response
            (update-in [:body :session] dissoc :current-user-id)
            (update-in [:body :session] dissoc :current-user))))))


(defn wrap-session
  "For the inbound requests, takes the session and puts it into the body params for
   use directly in the query/command. For the outbound response, ensures that the
   session in the body is only passed on if it is non empty."
  [handler]
  (fn [{:keys [session] :as request}]
    (let [request (assoc-in request [:body-params :session] session)
          response (handler request)
          session (get-in response [:body :session])]
      (if (empty? session)
        (assoc response :session nil)
        (assoc response :session session)))))


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
  "Determines the validity of the content provided by the client, and the validity of
   the content returned to the client."
  [handler]
  (fn [{:keys [body-params] :as request}]
    (if (s/valid? :request/body-params body-params)
      (update (handler request) :body (partial u/validate :response/body))
      (throw (IllegalArgumentException. "Invalid request content.")))))


(defn wrap-content-type
  "Formats the inbound request and outbound response based on the content type header."
  [handler]
  (fn [{:keys [headers] :as request}]
    (let [content-type (get-in request [:headers "content-type"])]
      (if (= content-type "application/transit+json")
        (try
          ((muuntaja.middleware/wrap-format handler) request)
          (catch clojure.lang.ExceptionInfo e
            (let [{:keys [type format]} (ex-data e)]
              (when (= :muuntaja/decode type)
                (throw (IllegalArgumentException. (str "Malformed " format " content.")))))))
        (throw (IllegalArgumentException. "Unsupported or missing Content-Type header."))))))


(defn wrap-request-path
  "Filters out any request path other than the root."
  [handler]
  (fn [{:keys [uri] :as request}]
    (if (or (= uri "/") (= uri ""))
      (handler request)
      (throw (IllegalArgumentException. "Unsupported request path.")))))


(defn wrap-request-method
  "Filters out any request method other than POST."
  [handler]
  (fn [{:keys [request-method] :as request}]
    (if (= request-method :post)
      (handler request)
      (throw (IllegalArgumentException. "Unsupported request method.")))))


(defn wrap-exception
  "Handles all uncaught exceptions."
  [handler]
  (fn [request]
    (try
      (handler request)
      (catch IllegalArgumentException e
        {:status 400
         :headers {"Content-Type" "application/json; charset=utf-8"}
         :body (str "{\"error\": \"" (.getMessage e) "\"}")})
      (catch Exception e
        {:status 500
         :headers {"Content-Type" "application/json; charset=utf-8"}
         :body "{\"error\": \"Internal error detected.\"}"}))))


(defn wrap-cors
  "Handles the cross origin resource sharing concerns."
  [handler]
  (cors.middleware/wrap-cors
   handler
   :access-control-allow-origin [(re-pattern (System/getenv "CORS_ORIGIN"))]
   :access-control-allow-methods [:options :post]
   :access-control-allow-headers ["Content-Type"]
   :access-control-allow-credentials "true"))
