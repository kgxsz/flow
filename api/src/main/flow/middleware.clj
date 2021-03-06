(ns flow.middleware
  (:require [flow.query :as query]
            [flow.command :as command]
            [medley.core :as medley]
            [ring.middleware.cors :as cors.middleware]
            [ring.middleware.session :as session.middleware]
            [ring.middleware.session.cookie :as cookie]
            [muuntaja.middleware :as muuntaja.middleware]))


(defn wrap-query-command-dispatch
  "Determines whether to dispatch to query/handle or command/handle,
   and adds it to the request to be used by the handler."
  [handler]
  (fn [{:keys [uri] :as request}]
    (case uri
      "/query" (handler (assoc request :handle query/handle))
      "/command" (handler (assoc request :handle command/handle))
      (throw (IllegalArgumentException. "Unsupported uri.")))))


(defn wrap-current-user-id
  "For inbound requests, takes the current user id found in the session
   and adds it to every query/command. For outbound responses, checks
   whether the current user id has been updated or removed, and acts
   accordingly by updating or removing the session."
  [handler]
  (fn [{:keys [body-params session] :as request}]
    (let [{:keys [current-user-id]} session
          body-params (medley/map-vals
                       #(assoc % :current-user-id current-user-id)
                       body-params)
          request (assoc request :body-params body-params)
          {:keys [body] :as response} (handler request)]
      (if (contains? body :current-user-id)
        (if-let [current-user-id (get-in response [:body :current-user-id])]
          (assoc-in response [:session :current-user-id] current-user-id)
          (assoc response :session nil))
        (assoc-in response [:body :current-user-id] current-user-id)))))


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
  "Determines the validity of the content provided by the client."
  [handler]
  (fn [{:keys [body-params] :as request}]
    (if (and (map? body-params) (not (empty? body-params)))
      (handler request)
      (throw (IllegalArgumentException. "Unsupported content.")))))


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


(defn wrap-request-method
  "Filters out any request method other than POST."
  [handler]
  (fn [{:keys [request-method] :as request}]
    (if (= request-method :post)
      (handler request)
      (throw (IllegalArgumentException. "Unsupported request method.")))))


(defn wrap-cors
  "Handles the cross origin resource sharing concerns."
  [handler]
  (cors.middleware/wrap-cors
   handler
   :access-control-allow-origin [(re-pattern (System/getenv "CORS_ORIGIN"))]
   :access-control-allow-methods [:options :post]
   :access-control-allow-headers ["Content-Type"]
   :access-control-allow-credentials "true"))


(defn wrap-exception
  "Handles all uncaught exceptions."
  [handler]
  (fn [request]
    (try
      (handler request)
      (catch IllegalArgumentException e
        {:status 400
         :headers {"Content-Type" "application/json; charset=utf-8"}
         :body (str "{\"error\": \"" (.getMessage e) "\"}")}))))
