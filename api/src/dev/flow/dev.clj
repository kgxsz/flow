(ns flow.dev
  (:require [ring.adapter.jetty :as jetty]
            [flow.middleware :as middleware]
            [flow.specifications :as specifications]
            [flow.core :as core]
            [flow.db :as db]
            [flow.entity.authorisation :as authorisation]
            [flow.entity.user :as user]
            [flow.entity.user :as user]
            [taoensso.faraday :as faraday]
            [kaocha.repl :as kaocha]))


(defn server []
  (let [options {:port 80
                 :ssl-port 443
                 :join? false
                 :ssl? true
                 :keystore "ssl/keystore.jks"
                 :key-password (System/getenv "KEYSTORE_PASSWORD")}]
   (jetty/run-jetty #'core/handler options)))


(defn create-table
  []
  (let [table-description (faraday/describe-table db/config :flow)
        table-index [:partition :s]
        table-options {:throughput {:read 1 :write 1} :block? true}]
    (when (some? table-description)
      (faraday/delete-table db/config :flow))
    (faraday/create-table db/config :flow table-index table-options)))


(defn seed-table []
  (let [users [["k.suzukawa@gmail.com" "Keigo" #{:admin :customer}]
               ["ksarnecka50@gmail.com" "Kasia" #{:customer}]]]
    (doall (map (partial apply user/create!) users))))


(comment

  (server)

  (create-table)

  (seed-table)

  (kaocha/run :unit)

  (kaocha/run :feature)

  (user/create! "success+9@simulator.amazonses.com" "Test" #{:customer})

  (user/create! "k.suzukawa@gmail.com" "Keigo" #{:admin :customer})

  (user/create! "ksarnecka50@gmail.com" "Kasia" #{:customer})

  (user/fetch (user/id "k.suzukawa@gmail.com"))

  (user/fetch-all)

  (authorisation/fetch-all)

  (doall
   (for [i (range 5000)]
     (user/create! (str "jimmy+" i "@gmail.com") (str "Jimmy+" i) #{:admin :customer})))

  (count (user/fetch-all))

  (meta (faraday/scan
        {:access-key (System/getenv "ACCESS_KEY")
         :secret-key (System/getenv "SECRET_KEY")
         :endpoint (System/getenv "DB_ENDPOINT")
         :batch-write-limit 25}
        :flow
        {:attr-conds {:partition [:begins-with :user]}
         #_:last-prim-kvs #_{:partition "user:d36ab9e3-cda5-5960-8fe2-8e6b9b603f66"}}))

)
