(ns flow.domain.authorisation-attempt
  (:require [flow.entity.authorisation :as authorisation]
            [clj-time.coerce :as t.coerce]
            [clj-time.core :as t]
            [hiccup.core :as hiccup]
            [clojure.java.io :as io]))


(defn grant
  "Marks the authorisation as granted by specifying
   the time of grant as now, if and only if the
   authorisation has not previously been granted."
  [{:keys [authorisation/granted-at] :as authorisation}]
  (cond-> authorisation
    (nil? granted-at)
    (assoc :authorisation/granted-at (t.coerce/to-date (t/now)))))


(defn grantable?
  "Given an authorisation, determines if it is grantable.
   An authorisation is grantable if it hasn't already been
   granted, and if it was created less than 5 minutes ago."
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


(defn email
  "Generates the authorisation email."
  [phrase]
  {:subject "Complete your sign in"
   :body {:html (hiccup/html
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
          :text (str "Use this magic phrase to complete your sign in: " phrase)}})

