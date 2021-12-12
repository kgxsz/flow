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
  "User entity specific wrapper."
  [id]
  (db/fetch-entity :user id))


(defn fetch-all
  "User entity specific wrapper."
  [limit offset]
  (db/fetch-entities :user limit offset))


(defn create!
  "Creates a new user entity."
  [email-address name roles]
  (let [now (t.coerce/to-date (t/now))
        id (id email-address)]
    (db/create-entity!
     :user
     id
     {:user/id id
      :user/email-address email-address
      :user/name name
      :user/roles roles
      :user/created-at now
      :user/deleted-at nil})))


(defn mutate!
  "User entity specific wrapper."
  [id f]
  (db/mutate-entity! :user id f))


(defn index
  "User entity specific wrapper."
  [& users]
  (u/index-entities :user/id users))
