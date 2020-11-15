(ns flow.middleware
  (:require [flow.query :as query]
            [flow.command :as command]
            [ring.util.response :as response]
            [ring.middleware.cors :as cors.middleware]
            [muuntaja.middleware :as muuntaja.middleware]
            [medley.core :as medley]
            [clojure.core.match :as match]
            [clojure.java.io :as io]))


(defn wrap-handle
  "Determines whether to use the query/handle or command/handle function,
   and adds it to the request to be used by the handler."
  [handler]
  (fn [{:keys [request-method uri] :as request}]
    (match/match
     [request-method uri]
     [:post "/query"] (handler (assoc request :handle query/handle))
     [:post "/command"] (handler (assoc request :handle command/handle))
     [_ _] (throw (IllegalArgumentException. "Unsupported method and/or uri.")))))


(defn wrap-content-type
  "Formats the inbound request and outbound response based on the content type header."
  [handler]
  (muuntaja.middleware/wrap-format handler))


(defn wrap-cors
  "Handles all the cross origin resource sharing concerns."
  [handler]
  (cors.middleware/wrap-cors
   handler
   :access-control-allow-origin [(re-pattern "http://localhost:8080")]
   :access-control-allow-methods [:options :post]
   :access-control-allow-credentials "true"))


(defn wrap-exception
  "Handles all uncaught exceptions, prints the stacktrace to the logs,
   and then returns the appropriate error code."
  [handler]
  (fn [request]
    (try
      (handler request)
      (catch IllegalArgumentException e
        (response/bad-request {:error (.getMessage e)})))))


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
       :body (slurp body)})))
