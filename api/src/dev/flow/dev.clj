(ns flow.dev
  (:require [flow.middleware :as middleware]
            [medley.core :as medley]
            [ring.util.response :as response]
            [ring.adapter.jetty :as jetty]))


(def handler
  (-> (fn [request]
        (->> (:body-params request)
             (map (:handle request))
             (apply medley/deep-merge)
             (response/response)))
      (middleware/wrap-handle)
      (middleware/wrap-content-type)
      (middleware/wrap-method)
      (middleware/wrap-cors)
      (middleware/wrap-exception)))


(defn server []
  (let [options {:port 80 :join? false}]
    (jetty/run-jetty #'handler options)))
