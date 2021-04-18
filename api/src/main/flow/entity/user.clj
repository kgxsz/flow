(ns flow.entity.user
  (:require [flow.db :as db]
            [flow.entity.utils :as u]
            [clj-time.coerce :as t.coerce]
            [clj-time.core :as t]
            [clj-uuid :as uuid]))


(defn id
  "Generates a deterministic user id based on the email address."
  [email-address]
  (uuid/v5 #uuid "0cb29677-4eaf-578f-ab9b-f9ac67c33cb9"
           {:email-address email-address}))


(defn fetch
  "Fetches the user at the given id."
  [id]
  (db/fetch-entity :user id))


(defn fetch-all
  "Fetches all users."
  []
  (db/fetch-entities :user))


(defn create !
  "Creates a new user."
  [email-address name roles]
  (let [now (t.coerce/to-date (t/now))
        id (id email-address)]
    (db/put-entity!
     :user
     id
     {:user/id id
      :user/email-address email-address
      :user/name name
      :user/roles roles
      :user/created-at now
      :user/deleted-at nil})))


(defn mutate!
  "Mutates the user at the given id by applying the given function."
  [id f]
  (db/mutate-entity! :user id f))


(defn index-user
  "Returns a map with key equal to the id of the provided user,
   and value equal to the user itself."
  [user]
  (u/index-entity :user/id user))


(defn index-users
  "Returns a map with keys equal to the ids of the provided users,
   and values equal to the users themselves."
  [users]
  (u/index-entities :user/id users))


(defn select-default-accessible-keys
  "Returns the provided user with only the default accessible keys present."
  [user]
  (let [keys []]
    (u/select-default-accessible-keys keys user)))


(defn select-owner-accessible-keys
  "Returns the provided user with only the owner accessible keys present."
  [current-user user]
  (let [keys [:user/id
              :user/email-address
              :user/name
              :user/roles
              :user/created-at
              :user/deleted-at]]
    (u/select-owner-accessible-keys keys current-user user)))


(defn select-role-accessible-keys
  "Returns the provided user with only the role accessible keys present."
  [current-user user]
  (let [keys {:admin [:user/id
                      :user/email-address
                      :user/name
                      :user/roles
                      :user/created-at
                      :user/deleted-at]
              :customer [:user/id
                         :user/name
                         :user/created-at
                         :user/deleted-at]}]
    (u/select-role-accessible-keys keys current-user user)))
