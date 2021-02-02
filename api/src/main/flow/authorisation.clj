(ns flow.authorisation
  (:require [hiccup.core :as hiccup]
            [ses-mailer.core :as mailer]))


(def email-address-whitelist
 #{"k.suzukawa@gmail.com"})


(defn whitelisted-email-address?
  [email-address]
  (contains? email-address-whitelist email-address))


(defn generate-magic-phrase
  []
  "banana-hat-cup")



(defn send-authorisation-email
  [email-address magic-phrase]
  (mailer/send-email
   {}
   "Flow <noreply@flow.keigo.io>"
   email-address
   "Complete your sign in"
   {:html-body (hiccup/html
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
    :text-body (str "Use this magic phrase to complete your sign in: " magic-phrase)}))
