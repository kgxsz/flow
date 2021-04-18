(ns flow.core
  (:require [flow.query :as query]
            [flow.command :as command]
            [muuntaja.core :as muuntaja]
            [flow.middleware :as middleware]
            [flow.specifications :as specifications]
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


(defn handle-command
  "Fulfills each command method and merges the outcomes into a result.
   Outputs a map containing the yet to be fulfilled query, and the metadata
   provided merged with any metadata present in the command result."
  [{:keys [query command metadata]}]
  (let [handle (fn [[method payload]] (command/handle method payload metadata))
        result (apply medley/deep-merge (map handle command))]
    {:query query
     :metadata (medley/deep-merge metadata (get result :metadata {}))}))


(defn resolve-ids
  "Takes the ID resolution map provided in the metadata and replaces any
   temporary ID in the query with its counterpart non temporary ID produced
   in the command. This is needed because the app cannot know an entity's ID
   before it is created, so it provides a temporary one that requires resolving."
  [{:keys [query metadata]}]
  {:query (clojure.walk/postwalk
           #(get (:id-resolution metadata) % %)
           query)
   :metadata metadata})


(defn handle-query
  "Fulfills each query method and merges the outcomes into a result.
   Outputs a map containing each entity, and the metadata provided
   merged with any metadata present in the query result."
  [{:keys [query metadata]}]
  (let [handle (fn [[method payload]] (query/handle method payload metadata))
        result (apply medley/deep-merge (map handle query))]
    {:users (get result :users {})
     :authorisations (get result :authorisations {})
     :metadata (medley/deep-merge metadata (get result :metadata {}))}))


(def handler
  (-> (fn [{:keys [body-params]}]
        (-> body-params
            (handle-command)
            (resolve-ids)
            (handle-query)
            (response/response)))
      (middleware/wrap-access-control)
      (middleware/wrap-current-user)
      (middleware/wrap-session)
      (middleware/wrap-content-validation)
      (middleware/wrap-content-type)
      (middleware/wrap-request-path)
      (middleware/wrap-request-method)
      (middleware/wrap-exception)
      (middleware/wrap-cors)))


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
        :body "{\"error\": \"Internal error detected.\"}"}))))
