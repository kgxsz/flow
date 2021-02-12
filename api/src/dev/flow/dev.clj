(ns flow.dev
  (:require [ring.adapter.jetty :as jetty]
            [flow.middleware :as middleware]
            [flow.core :as core]
            [flow.db :as db]
            [flow.domain.authorisation :as authorisation]
            [flow.domain.user :as user]
            [taoensso.faraday :as faraday]))


(defn server []
  (let [options {:port 80
                 :ssl-port 443
                 :join? false
                 :ssl? true
                 :keystore "ssl/keystore.jks"
                 :key-password (System/getenv "KEYSTORE_PASSWORD")}]
   (jetty/run-jetty #'core/handler options)))


(defn seed []
  (let [table-index [:partition :s]
        table-options {:throughput {:read 1
                                    :write 1}
                       :block? true}
        users [{:email-address "k.suzukawa@gmail.com"
                :name "Keigo"
                :roles #{:admin :customer}}]]
    (faraday/create-table db/config :flow table-index table-options)
    (doall (map user/create users))))


(comment

  (server)

  (seed)

  (user/create {:email-address "k.suzukawa@gmail.com"
                :name "Keigo"
                :roles #{:admin :customer}})

  (user/create {:email-address "ksarnecka50@gmail.com"
                :name "Kasia"
                :roles #{:customer}})

  (user/fetch (user/id "ks.suzukawa@gmail.com"))

  (user/fetch-all)

  (authorisation/fetch (authorisation/id (user/id "k.suzukawa@gmail.com") "paste-work-belief"))

  (authorisation/fetch-all)

  )
