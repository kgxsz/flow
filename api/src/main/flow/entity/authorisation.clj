(ns flow.entity.authorisation
  (:require [flow.db :as db]
            [flow.entity.utils :as utils]
            [clj-uuid :as uuid]
            [clj-time.coerce :as t.coerce]
            [clj-time.core :as t]
            [medley.core :as medley]))


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
  "Fetches all authoirsations"
  []
  (db/fetch-entities :authorisation))


(defn create
  "Creates a new authorisation."
  [user-id phrase]
  (let [now (t.coerce/to-date (t/now))
        id (id user-id phrase)]
    (db/put-entity
     :authorisation
     id
     {:authorisation/id id
      :user/id user-id
      :authorisation/phrase phrase
      :authorisation/initialised-at now
      :authorisation/finalised-at nil})))


(defn update
  "Updates the authorisation at the given id by applying the given function."
  [id f]
  (db/update-entity :authorisation id f))


(defn filter-sanctioned-keys
  "Wraps the eponymous utility function with the
   authorisation entity specific sanctioned keys."
  [current-user authorisation]
  (let [sanctioned-keys
        {:default #{}
         :owner #{}
         :role {:admin #{:authorisation/id
                         :user/id
                         :authorisation/phrase
                         :authorisation/initialised-at
                         :authorisation/finalised-at}
                :customer #{}}}]
    (utils/filter-sanctioned-keys
     sanctioned-keys
     current-user
     authorisation)))
