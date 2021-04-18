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
  "Fetches the authorisation at the given id."
  [id]
  (db/fetch-entity :authorisation id))


(defn fetch-all
  "Fetches all authorisations."
  []
  (db/fetch-entities :authorisation))


(defn create!
  "Creates a new authorisation."
  [user-id phrase]
  (let [now (t.coerce/to-date (t/now))
        id (id user-id phrase)]
    (db/put-entity!
     :authorisation
     id
     {:authorisation/id id
      :user/id user-id
      :authorisation/phrase phrase
      :authorisation/created-at now
      :authorisation/granted-at nil})))


(defn mutate!
  "Mutates the authorisation at the given id by applying the given function."
  [id f]
  (db/mutate-entity! :authorisation id f))


(defn index-authorisation
  "Returns a map with key equal to the id of the provided authorisation,
   and value equal to the authorisation itself."
  [authorisation]
  (u/index-entity :authorisation/id authorisation))


(defn index-authorisations
  "Returns a map with keys equal to the ids of the provided authorisations,
   and values equal to the authorisations themselves."
  [authorisations]
  (u/index-entities :authorisation/id authorisations))


(defn select-default-accessible-keys
  "Returns the provided authorisation with only the default accessible keys present."
  [authorisation]
  (let [keys []]
    (u/select-default-accessible-keys keys authorisation)))


(defn select-owner-accessible-keys
  "Returns the provided authorisation with only the owner accessible keys present."
  [current-user authorisation]
  (let [keys []]
    (u/select-owner-accessible-keys keys current-user authorisation)))


(defn select-role-accessible-keys
  "Returns the provided authorisation with only the role accessible keys present."
  [current-user authorisation]
  (let [keys {:admin [:authorisation/id
                      :user/id
                      :authorisation/phrase
                      :authorisation/created-at
                      :authorisation/granted-at]
              :customer []}]
    (u/select-role-accessible-keys keys current-user authorisation)))
