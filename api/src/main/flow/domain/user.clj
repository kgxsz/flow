(ns flow.domain.user
  (:require [flow.db :as db]
            [clj-time.coerce :as t.coerce]
            [clj-time.core :as t]
            [clj-uuid :as uuid]
            [medley.core :as medley]))


(defn apply-visible-keys
  [{:keys [id] :as entity} {:keys [roles] :as current-user}]
  (let [visible-keys {:roles {:admin [:id :email-address :name :roles :created-at :deleted-at]
                              :customer [:id :name :created-at :deleted-at]}
                      :owner [:id :email-address :name :created-at :deleted-at]
                      :public []}
        filter-roles (comp vals (partial medley/filter-keys (partial contains? roles)))
        roles? (some? roles)
        owner? (= id (:id current-user))
        entity (select-keys
                entity
                (cond-> (:public visible-keys)
                  roles? (concat (filter-roles (:roles visible-keys)))
                  owner? (concat (:owner visible-keys))))]
    (when-not (empty? entity) entity)))


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
