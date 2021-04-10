(ns flow.middleware
  (:require [flow.entity.user :as user]
            [flow.utils :as u]
            [medley.core :as medley]
            [ring.middleware.cors :as cors.middleware]
            [ring.middleware.session :as session.middleware]
            [ring.middleware.session.cookie :as cookie]
            [muuntaja.middleware :as muuntaja.middleware]
            [clojure.spec.alpha :as s]))


(defn wrap-current-user
  "For inbound requests, takes the current user id found in the session and fetches
   the current user, then attaches it to the metadata. For outbound responses, checks
   whether the current user has been updated or removed, and acts accordingly by updating
   or removing the current user id in the session. Ensures that every response includes
   metadata with the current user id, nil or otherwise."
  [handler]
  (fn [{:keys [session] :as request}]
    (let [{:keys [user/id]} session
          request (assoc-in request [:body-params :metadata :current-user] (user/fetch id))
          {:keys [body] :as response} (handler request)]
      (if-let [id (get-in response [:body :metadata :current-user :user/id])]
        (-> response
            (assoc-in [:session :user/id] id)
            (assoc-in [:body :metadata :current-user-id] id)
            (update-in [:body :metadata] dissoc :current-user))
        (-> response
            (assoc-in [:session] nil)
            (assoc-in [:body :metadata :current-user-id] nil)
            (update-in [:body :metadata] dissoc :current-user))))))


(defn wrap-session
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
