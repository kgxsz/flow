(ns flow.entity.authorisation
  (:require [flow.db :as db]
            [flow.entity.utils :as u]
            [clj-uuid :as uuid]
            [clj-time.coerce :as t.coerce]
            [clj-time.core :as t]))


(defn id
  "Generates a deterministic user id based on the user id and the phrase."
  [user-id phrase]
  (uuid/v5 #uuid "2f636b80-6935-11eb-8e66-4838500ac459"
           {:user-id user-id
            :phrase phrase}))


(defn fetch
  "Authorisation entity specific wrapper."
  [id]
  (db/fetch-entity :authorisation id))


(defn fetch-all
  "Authorisation entity specific wrapper."
  [limit offset]
  (db/fetch-entities :authorisation limit offset))


(defn create!
  "Creates a new authorisation entity."
  [user-id phrase]
  (let [now (t.coerce/to-date (t/now))
        id (id user-id phrase)]
    (db/create-entity!
     :authorisation
     id
     {:authorisation/id id
      :user/id user-id
      :authorisation/phrase phrase
      :authorisation/created-at now
      :authorisation/granted-at nil})))


(defn mutate!
  "Authorisation entity specific wrapper."
  [id f]
  (db/mutate-entity! :authorisation id f))


(defn index
  "Authorisation entity specific wrapper."
  [& authorisations]
  (u/index-entities :authorisation/id authorisations))


(defn select-default-accessible-keys
  "Authorisation entity specific wrapper."
  [authorisation]
  (let [keys []]
    (u/select-default-accessible-keys keys authorisation)))


(defn select-owner-accessible-keys
  "Authorisation entity specific wrapper."
  [current-user authorisation]
  (let [keys []]
    (u/select-owner-accessible-keys keys current-user authorisation)))


(defn select-role-accessible-keys
  "Authorisation entity specific wrapper."
  [current-user authorisation]
  (let [keys {:admin [:authorisation/id
                      :user/id
                      :authorisation/phrase
                      :authorisation/created-at
                      :authorisation/granted-at]
              :customer []}]
    (u/select-role-accessible-keys keys current-user authorisation)))
