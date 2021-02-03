(ns flow.domain.authorisation
  (:require [hiccup.core :as hiccup]
            [clojure.java.io :as io]
            [flow.email :as email]))


(def email-address-whitelist
 #{"k.suzukawa@gmail.com"})


(defn whitelisted-email-address?
  [email-address]
  (contains? email-address-whitelist email-address))


(defn generate-magic-phrase
  []
  (->> (io/resource "words.edn")
       (slurp)
       (clojure.edn/read-string)
       (partial rand-nth)
       (repeatedly 3)
       (interpose "-")
       (apply str)))


(defn send-authorisation-email
  [email-address]
  (if (whitelisted-email-address? email-address)
    (let [magic-phrase (generate-magic-phrase)]
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
            magic-phrase]]]])
       (str "Use this magic phrase to complete your sign in: " magic-phrase)))))
