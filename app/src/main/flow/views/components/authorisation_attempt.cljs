(ns flow.views.components.authorisation-attempt
  (:require [re-frame.core :as re-frame]
            [flow.views.widgets.input :as input]
            [flow.views.widgets.button :as button]
            [flow.utils :as u]))


(defn view [{:keys [status]}
            {:keys [input
                    button]}
            {:keys [update-email-address
                    start-initialisation
                    update-phrase
                    start-finalisation]}]

  (case status
    (:idle
     :initialisation-pending
     :initialisation-error)
    [:div
     {:key status
      :class (u/bem [:authorisation-attempt])}
     [:div
      {:class (u/bem [:text :align-center :padding-top-medium])}
      "Sign in with your email address."]
     [input
      {:subscriptions {:value :authorisation-attempt/email-address
                       :disabled? :authorisation-attempt/email-address-update-disabled?}
       :placeholder "jane@smith.com"
       :icon :envelope}
      {:on-change update-email-address}]
     [:div
      {:class (u/bem [:cell :row :margin-top-small])}
      [button
       {:subscriptions {:disabled? :authorisation-attempt/initialisation-disabled?
                        :pending? :authorisation-attempt/initialisation-pending?}
        :label "Continue"
        :icon :arrow-right}
       {:on-click start-initialisation}]]
     (when (= status :initialisation-error)
       [:div
        {:class (u/bem [:cell :row :padding-top-small])}
        [:div
         {:class (u/bem [:icon :font-size-medium :warning])}]
        [:div
         {:class (u/bem [:text :font-size-small :padding-left-tiny])}
         "Something has gone wrong"]])]

    (:initialisation-successful
     :finalisation-pending
     :finalisation-successful
     :finalisation-unsuccessful
     :finalisation-error)
    [:div
     {:key status
      :class (u/bem [:authorisation-attempt])}
     [:div
      {:class (u/bem [:text :align-center :padding-top-medium])}
      "We've emailed you a magic phrase."]
     [input
      {:subscriptions {:value :authorisation-attempt/phrase
                       :disabled? :authorisation-attempt/phrase-update-disabled?}
       :placeholder "donkey-purple-kettle"
       :icon :magic-wand}
      {:on-change update-phrase}]
     [:div
      {:class (u/bem [:cell :row :margin-top-small])}
      [button
       {:subscriptions {:disabled? :authorisation-attempt/finalisation-disabled?
                        :pending? :authorisation-attempt/finalisation-pending?}
        :label "Sign in"
        :icon :arrow-right}
       {:on-click start-finalisation}]]
     (when (= status :finalisation-unsuccessful)
       [:div
        {:class (u/bem [:cell :row :padding-top-small])}
        [:div
         {:class (u/bem [:icon :font-size-medium :warning])}]
        [:div
         {:class (u/bem [:text :font-size-small :padding-left-tiny])}
         "That magic phrase doesn't look right."]])
     (when (= status :finalisation-error)
       [:div
        {:class (u/bem [:cell :row :padding-top-small])}
        [:div
         {:class (u/bem [:icon :font-size-medium :warning])}]
        [:div
         {:class (u/bem [:text :font-size-small :padding-left-tiny])}
         "Something has gone wrong!"]])]))


(defn authorisation-attempt []
  (let [!status (re-frame/subscribe [:authorisation-attempt/status])]
    (fn []
      [view
       {:status @!status}
       {:input input/input
        :button button/primary-button}
       {:update-email-address
        #(re-frame/dispatch [:authorisation-attempt/email-address-updated %])
        :start-initialisation
        #(re-frame/dispatch [:authorisation-attempt/initialisation-started])
        :update-phrase
        #(re-frame/dispatch [:authorisation-attempt/phrase-updated %])
        :start-finalisation
        #(re-frame/dispatch [:authorisation-attempt/finalisation-started])}])))
