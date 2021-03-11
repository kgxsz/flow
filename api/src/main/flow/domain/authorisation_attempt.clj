(ns flow.domain.authorisation-attempt
  (:require [flow.entity.authorisation :as authorisation]
            [clj-time.coerce :as t.coerce]
            [clj-time.core :as t]
            [hiccup.core :as hiccup]
            [clojure.java.io :as io]
            [flow.email :as email]))


(defn finalise
  "Marks the authorisation with the given id as finalised now."
  [id]
  (authorisation/update
   id
   #(cond-> %
      (nil? (:authorisation/finalised-at %))
      (assoc :authorisation/finalised-at (t.coerce/to-date (t/now))))))


(defn expired?
  "Given an authoirsation, determines if it is expired. "
  [{:authorisation/keys [initialised-at]}]
  (-> (t.coerce/from-date initialised-at)
      (t/plus (t/minutes 5))
      (t/before? (t/now))) )


(defn generate-phrase
  "Generates a phrase of three words."
  []
  (->> (io/resource "words.edn")
       (slurp)
       (clojure.edn/read-string)
       (partial rand-nth)
       (repeatedly 3)
       (interpose "-")
       (apply str)))


(defn send-phrase
  "Sends the given phrase to the given email address"
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

