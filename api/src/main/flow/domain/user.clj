(ns flow.domain.user
  (:require [flow.db :as db]
            [clj-time.coerce :as t.coerce]
            [clj-time.core :as t]
            [clj-uuid :as uuid]))


(defn strip
  [user {:keys [roles] :as current-user}]
  (let [admin [:id :email-address :name :roles :created-at :deleted-at]
        customer [:id :name :created-at :deleted-at]
        self [:id :email-address :name :created-at :deleted-at]
        public []]
    (if (nil? current-user)
      (let [x (select-keys user public)]
        (when-not (empty? x) x))
      (let [x (if (= current-user user)
                (merge (select-keys user self)
                       (select-keys user (when (contains? roles :admin) admin))
                       (select-keys user (when (contains? roles :customer) customer)))
                (merge (select-keys user (when (contains? roles :admin) admin))
                       (select-keys user (when (contains? roles :customer) customer))))]
        (when-not (empty? x) x)))))


(defn id [email-address]
  (uuid/v5 #uuid "0cb29677-4eaf-578f-ab9b-f9ac67c33cb9"
           {:email-address email-address}))


(defn fetch [id]
  (db/fetch-entity :user id))


(defn fetch-all []
  (db/fetch-entities :user))


(defn create [{:keys [email-address name roles]}]
  (let [now (t.coerce/to-date (t/now))
        id (id email-address)]
    (when (nil? (fetch id))
      (db/put-entity :user
                     {:id id
                      :email-address email-address
                      :name name
                      :roles roles
                      :created-at now
                      :deleted-at nil}))))
