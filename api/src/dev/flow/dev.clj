(ns flow.dev
  (:require [ring.adapter.jetty :as jetty]
            [flow.core :as core]))


(defn server []
  (let [options {:port 80 :join? false}]
    (jetty/run-jetty #'core/handler options)))
