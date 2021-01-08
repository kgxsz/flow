(ns flow.middleware
  (:require [flow.query :as query]
            [flow.command :as command]
            [ring.middleware.cors :as cors.middleware]
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
      (if (or (= content-type "application/json")
              (= content-type "application/transit+json"))
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
