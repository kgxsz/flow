(ns flow.dev
  (:require [flow.middleware :as middleware]
            [ring.adapter.jetty :as jetty]
            [flow.core :as core]))


(def wrapped-handler
  (-> core/handler
      (middleware/wrap-query-command-dispatch)
      (middleware/wrap-content-validation)
      (middleware/wrap-content-type)
      (middleware/wrap-request-method)
      (middleware/wrap-cors)
      (middleware/wrap-exception)))


(defn server []
  (let [options {:port 80 :join? false}]
    (jetty/run-jetty #'wrapped-handler options)))
