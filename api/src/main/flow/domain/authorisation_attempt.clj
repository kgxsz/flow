(ns flow.domain.authorisation-attempt
  (:require [flow.entity.authorisation :as authorisation]
            [clj-time.coerce :as t.coerce]
            [clj-time.core :as t]
            [hiccup.core :as hiccup]
            [clojure.java.io :as io]
            [flow.email :as email]))


(defn grant!
  "Marks the authorisation with the given id as granted now."
  [id]
  (authorisation/mutate!
   id
   #(cond-> %
      (nil? (:authorisation/granted-at %))
      (assoc :authorisation/granted-at (t.coerce/to-date (t/now))))))


(defn grantable?
  "Given an authorisation, determines if it is grantable.
   An authorisation is grantable if it hasn't already been
   granted, and if it was created in the last 5 minutes."
  [{:authorisation/keys [granted-at created-at]}]
  (and
   (nil? granted-at)
   (-> (t.coerce/from-date created-at)
       (t/plus (t/minutes 5))
       (t/after? (t/now)))))


(defn generate-phrase
  "Generates a phrase of three hyphenated words."
  []
  (->> (io/resource "words.edn")
       (slurp)
       (clojure.edn/read-string)
       (partial rand-nth)
       (repeatedly 3)
       (interpose "-")
       (apply str)))


(defn send-phrase!
  "Sends the given phrase to the given email address"
  [email-address phrase]
  (email/send-email!
   email-address
   "Complete your sign in"
   {:html (hiccup/html
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
    :text (str "Use this magic phrase to complete your sign in: " phrase)}))

