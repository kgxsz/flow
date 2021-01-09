(ns flow.dev
  (:require [ring.adapter.jetty :as jetty]
            [flow.middleware :as middleware]
            [flow.core :as core]))


(defn server []
  (let [options {:port 80
                 :ssl-port 443
                 :join? false
                 :ssl? true
                 :keystore "ssl/keystore.jks"
                 :key-password "api.localhost"}]
   (jetty/run-jetty #'core/handler options)))
