(ns flow.command
  (:require [ses-mailer.core :as mailer]
            [hiccup.core :as hiccup]))


(defmulti handle first)


(defmethod handle :example [command]
  {})


(defmethod handle :initialise-authorisation [[_ {:keys [email-address]}]]
  (let [email-address-whitelist #{"k.suzukawa@gmail.com"}
        magic-phrase "banana-hat-cup"]
    (if (contains? email-address-whitelist email-address)
      (try
        (mailer/send-email
         {}
         "Flow <noreply@flow.keigo.io>"
         email-address
         "Complete your sign in to Flow"
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
          :text-body (str "Use this magic phrase to complete your sign in: " magic-phrase)})
        {}
        (catch Exception e
          {}))
      {})))


(defmethod handle :finalise-authorisation [[_ {:keys [phrase]}]]
  (if (= phrase "banana-hat-cup")
    {:current-user-id 1101}
    {}))


(defmethod handle :deauthorise [command]
  {:current-user-id nil})


(defmethod handle :default [command]
  (throw (IllegalArgumentException. "Unsupported command method.")))
