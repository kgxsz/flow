(ns flow.core
  (:require [muuntaja.core :as muuntaja]
            [flow.middleware :as middleware]
            [ring.util.response :as response]
            [clojure.java.io :as io]
            [medley.core :as medley])
  (:gen-class
   :implements [com.amazonaws.services.lambda.runtime.RequestStreamHandler]))


(defn read-input-stream
  "Decodes the Lambda compliant input from an input stream."
  [input-stream]
  (muuntaja/decode "application/json" input-stream))


(defn adapt-input
  "Adapts the Lambda compliant input into a ring compliant request."
  [{:keys [headers path requestContext httpMethod queryStringParameters body]}]
  (let [{:keys [X-Forwarded-Port X-Forwarded-For X-Forwarded-Proto Host]} headers]
    {:server-port (some-> X-Forwarded-Port
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
     :request-method (-> httpMethod
                         (clojure.string/lower-case)
                         (keyword))
     :body (some-> body (.getBytes) io/input-stream)
     :query-string queryStringParameters}))


(defn write-output-stream
  "Encodes the Lambda compliant output into an output stream."
  [output-stream response]
  (let [encoder (muuntaja/create (assoc muuntaja/default-options :return :bytes))]
    (.write output-stream (muuntaja/encode encoder "application/json" response))))


(defn adapt-output
  "Adapts the ring compliant response into a Lambda compliant output."
  [{:keys [status headers body]}]
  {:statusCode status
   :headers (medley/remove-vals coll? headers)
   :multiValueHeaders (medley/filter-vals coll? headers)
   :body (try (slurp body) (catch Exception e body))})


(def handler
  (-> (fn [request]
        (->> (:body-params request)
             (map (:handle request))
             (apply medley/deep-merge)
             (response/response)))
      (middleware/wrap-query-command-dispatch)
      (middleware/wrap-current-user)
      (middleware/wrap-session)
      (middleware/wrap-content-validation)
      (middleware/wrap-content-type)
      (middleware/wrap-request-method)
      (middleware/wrap-cors)
      (middleware/wrap-exception)))


(defn -handleRequest
  "Handles the Lambda invokation lifecycle."
  [_ input-stream output-stream context]
  (try
    (->> input-stream
         (read-input-stream)
         (adapt-input)
         (handler)
         (adapt-output)
         (write-output-stream output-stream))
    (catch Exception e
      (.printStackTrace e)
      (write-output-stream
       output-stream
       {:statusCode 500
        :headers {"Content-Type" "application/json; charset=utf-8"}
        :body "{\"error\": \"Uncaught exception.\"}"}))))
