(ns flow.domain.user
  (:require [flow.db :as db]
            [clj-time.coerce :as t.coerce]
            [clj-time.core :as t]
            [clj-uuid :as uuid]))


(defn id [email-address]
  (uuid/v5 #uuid "0cb29677-4eaf-578f-ab9b-f9ac67c33cb9"
           {:email-address email-address}))


(defn fetch [id]
  (db/fetch-entity :user id))


(defn fetch-all []
  (db/fetch-entities :user))


(defn create [{:keys [email-address name]}]
  (let [now (t.coerce/to-date (t/now))
        id (id email-address)]
    (when (nil? (fetch id))
      (db/put-entity :user
                     {:id id
                      :name name
                      :email-address email-address
                      :created-at now
                      :deleted-at nil}))))
