(ns flow.domain.user
  (:require [flow.db :as db]
            [flow.domain.utils :as utils]
            [clj-time.coerce :as t.coerce]
            [clj-time.core :as t]
            [clj-uuid :as uuid]
            [medley.core :as medley]))


(defn convey-keys [current-user entity]
  (let [conveyable-keys {:roles {:admin [:user/id
                                         :user/email-address
                                         :user/name
                                         :user/roles
                                         :user/created-at
                                         :user/deleted-at]
                                 :customer [:user/id
                                            :user/name
                                            :user/created-at
                                            :user/deleted-at]}
                         :owner [:user/id
                                 :user/email-address
                                 :user/name
                                 :user/roles
                                 :user/created-at
                                 :user/deleted-at]
                         :public []}]
    (utils/convey-keys conveyable-keys current-user entity)))


(defn id [email-address]
  (uuid/v5 #uuid "0cb29677-4eaf-578f-ab9b-f9ac67c33cb9"
           {:email-address email-address}))


(defn fetch [id]
  (db/fetch-entity :user id))


(defn fetch-all []
  (db/fetch-entities :user))


(defn create [email-address name roles]
  (let [now (t.coerce/to-date (t/now))
        id (id email-address)]
    (when (nil? (fetch id))
      (db/put-entity
       :user
       id
       {:user/id id
        :user/email-address email-address
        :user/name name
        :user/roles roles
        :user/created-at now
        :user/deleted-at nil}))))


(defn delete [id]
  (when-let [{:keys [user/deleted-at]} (fetch id)]
    (when (nil? deleted-at)
      (db/update-entity
       :user
       id
       #(assoc % :user/deleted-at (t.coerce/to-date (t/now)))))))


(defn admin?
  [id]
  (contains? (:user/roles (fetch id)) :admin))
