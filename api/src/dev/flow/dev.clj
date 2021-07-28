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

  (kaocha/run 'flow.query.users-test)

  (user/create! "success+9@simulator.amazonses.com" "Test" #{:customer})

  (user/create! "k.suzukawa@gmail.com" "Keigo" #{:admin :customer})

  (user/create! "ksarnecka50@gmail.com" "Kasia" #{:customer})

  (user/fetch (user/id "k.suzukawa@gmail.com"))

  (user/fetch-all)

  (authorisation/fetch-all)

  ;; - Add query feature tests
  ;;   - users query
  ;;     - No session
  ;;     - Unauthorised session
  ;;     - Authorised session with customer role
  ;;     - Authorised session with admin role

  ;; The users query relies on the state of the DB, since the testing DB is
  ;; shared between both feature tests and regular local development, it means
  ;; that you're going to have trouble. So, there's two viable options here:
  ;; 1. Separate local DBs and have one for feature tests and one for local development
  ;; 2. Make the feature tests nuke the local DB every time, and make it easy to seed
  ;;    the DB for local development.
  ;; I like options two since it's the simpler one for now and the local DB is in memory
  ;; anyway so it's not inconceivable for the data in there to be nuked.

  ;; Collect all the TODO items in a single place.
)
