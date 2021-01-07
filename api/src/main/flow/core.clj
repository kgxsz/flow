(ns flow.core
  (:require [muuntaja.core :as muuntaja]
            [flow.middleware :as middleware]
            [ring.util.response :as response]
            [medley.core :as medley])
  (:gen-class
   :implements [com.amazonaws.services.lambda.runtime.RequestStreamHandler]))


(defn read-input-stream
  [input-stream]
  (muuntaja/decode "application/json" input-stream))


(defn write-output-stream
  [output-stream response]
  (let [encoder (muuntaja/create (assoc muuntaja/default-options :return :bytes))]
    (.write output-stream (muuntaja/encode encoder "application/json" response))))


(defn handler
  [request]
  (->> (:body-params request)
       (map (:handle request))
       (apply medley/deep-merge)
       (response/response)))


(def wrapped-handler
  (-> handler
      (middleware/wrap-query-command-dispatch)
      (middleware/wrap-content-validation)
      (middleware/wrap-content-type)
      (middleware/wrap-request-method)
      (middleware/wrap-cors)
      (middleware/wrap-exception)
      (middleware/wrap-adaptor)))


(defn -handleRequest
  [_ input-stream output-stream context]
  (try
    (->> input-stream
         (read-input-stream)
         (wrapped-handler)
         (write-output-stream output-stream))
    (catch Exception e
      (.printStackTrace e)
      (write-output-stream
       output-stream
       {:statusCode 500
        :headers {"Content-Type" "application/json; charset=utf-8"}
        :body "{\"error\": \"Uncaught exception.\"}"}))))
