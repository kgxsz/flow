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


(defn seed []
  (let [table-index [:partition :s]
        table-options {:throughput {:read 1
                                    :write 1}
                       :block? true}
        users [["k.suzukawa@gmail.com" "Keigo" #{:admin :customer}]
               ["ksarnecka50@gmail.com" "Kasia" #{:customer}]]]
    (faraday/create-table db/config :flow table-index table-options)
    (doall (map (partial apply user/create!) users))))


(comment

  (server)

  (seed)

  (kaocha/run :unit)

  (kaocha/run :feature)

  (kaocha/run 'flow.command.finalise-authorisation-attempt-test)

  (user/create! "success+4@simulator.amazonses.com" "Test" #{:customer})

  (user/create! "k.suzukawa@gmail.com" "Keigo" #{:admin :customer})

  (user/create! "ksarnecka50@gmail.com" "Kasia" #{:customer})

  (user/fetch (user/id "k.suzukawa@gmail.com"))

  (user/fetch-all)

  (authorisation/fetch-all)

  ;; - Extract common feature test functions  [DONE]
  ;; - Extract helpers from other feature tests [DONE]
  ;; - Align arguments for authorisations
  ;; - Update helper function documentation
  ;; - Extract dummy data from unit tests
  ;; - Add more command feature tests
  ;; - Add qury feature tests

)
