(ns flow.domain.authorisation
  (:require [flow.db :as db]
            [flow.domain.utils :as utils]
            [clj-uuid :as uuid]
            [clj-time.coerce :as t.coerce]
            [clj-time.core :as t]
            [hiccup.core :as hiccup]
            [clojure.java.io :as io]
            [flow.email :as email]
            [medley.core :as medley]))


(defn generate-phrase
  []
  (->> (io/resource "words.edn")
       (slurp)
       (clojure.edn/read-string)
       (partial rand-nth)
       (repeatedly 3)
       (interpose "-")
       (apply str)))


(defn send-phrase
  [email-address phrase]
  (email/send-email
   email-address
   "Complete your sign in"
   (hiccup/html
    [:table {:width "100%"
             :height "250px"
             :border "0"
             :cellspacing "0"
             :cellpadding "0"}
     [:tr {:style "color: #333333"}
      [:td {:align "center"}
       [:div "Use this magic phrase to"]
       [:div "complete your sign in:"]
       [:div {:style "padding-top: 10px; font-size: 24px; font-weight: 700"}
        phrase]]]])
   (str "Use this magic phrase to complete your sign in: " phrase)))


(defn expired? [{:authorisation/keys [initialised-at]}]
  (-> (t.coerce/from-date initialised-at)
      (t/plus (t/minutes 5))
      (t/before? (t/now))))


(defn convey-keys [current-user entity]
  (let [conveyable-keys {:roles {:admin [:authorisation/id
                                         :user/id
                                         :authorisation/phrase
                                         :authorisation/initialised-at
                                         :authorisation/finalised-at]
                                 :customer []}
                         :owner []
                         :public []}]
    (utils/convey-keys conveyable-keys current-user entity)))


(defn id [user-id phrase]
  (uuid/v5 #uuid "2f636b80-6935-11eb-8e66-4838500ac459"
           {:user-id user-id
            :phrase phrase}))


(defn fetch [id]
  (db/fetch-entity :authorisation id))


(defn fetch-all []
  (db/fetch-entities :authorisation))


(defn create [user-id phrase]
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


(defn finalise [id]
  (db/update-entity
   :authorisation
   id
   #(cond-> %
      (nil? (:authorisation/finalised-at %))
      (assoc :authorisation/finalised-at (t.coerce/to-date (t/now))))))


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
