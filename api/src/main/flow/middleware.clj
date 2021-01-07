(ns flow.middleware
  (:require [flow.query :as query]
            [flow.command :as command]
            [ring.util.response :as response]
            [ring.middleware.cors :as cors.middleware]
            [muuntaja.middleware :as muuntaja.middleware]
            [medley.core :as medley]
            [clojure.java.io :as io]))


(defn wrap-handle
  "Determines whether to use the query/handle or command/handle function,
   and adds it to the request to be used by the handler."
  [handler]
  (fn [{:keys [uri] :as request}]
    (case uri
      "/query" (handler (assoc request :handle query/handle))
      "/command" (handler (assoc request :handle command/handle))
      (throw (IllegalArgumentException. "Unsupported uri.")))))


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


(defn wrap-method
  "Filters out any method other than POST."
  [handler]
  (fn [{:keys [request-method] :as request}]
    (if (= request-method :post)
      (handler request)
      (throw (IllegalArgumentException. "Unsupported method.")))))


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


(defn wrap-adaptor
  "Handles the adaption between Ring and AWS Lambda."
  [handler]
  (fn [{:keys [headers path requestContext body] :as request}]
    (let [{:keys [X-Forwarded-Port X-Forwarded-For X-Forwarded-Proto Host]} headers
          request {:server-port (some-> X-Forwarded-Port
                                        (Integer/parseInt))
                   :server-name Host
                   :remote-addr (some-> X-Forwarded-For
                                        (clojure.string/split #", ")
                                        (first))
                   :uri path
                   :scheme (keyword X-Forwarded-Proto)
                   :protocol (:protocol requestContext)
                   :headers (medley/map-keys
                             (comp clojure.string/lower-case name)
                             headers)
                   :request-method (-> (:httpMethod request)
                                       (clojure.string/lower-case)
                                       (keyword))
                   :body (some-> body (.getBytes) io/input-stream)
                   :query-string (:queryStringParameters request)}
          {:keys [status headers body] :as response} (handler request)]
      {:statusCode status
       :headers headers
       :body (try (slurp body) (catch Exception e body))})))
