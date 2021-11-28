(ns flow.views.processes.authorisation
  (:require [re-frame.core :as re-frame]
            [flow.views.widgets.input :as input]
            [flow.views.widgets.button :as button]
            [flow.utils :as u]))


(defn view [{:keys [status]}
            {:keys [email-address-input
                    start-initialisation-button
                    phrase-input
                    start-finalisation-button]}
            _]

  (case status

    (:idle :initialisation-pending)
    [:div
     {:key status
      :class (u/bem [:authorisation])}
     [:div
      {:class (u/bem [:text :align-center :padding-top-medium])}
      "Sign in with your email address"]
     email-address-input
     [:div
      {:class (u/bem [:cell :row :margin-top-small])}
      start-initialisation-button]]

    (:initialisation-successful
     :finalisation-pending
     :finalisation-successful
     :finalisation-unsuccessful)
    [:div
     {:key status
      :class (u/bem [:authorisation])}
     [:div
      {:class (u/bem [:text :align-center :padding-top-medium])}
      "We've emailed you a magic phrase"]
     phrase-input
     [:div
      {:class (u/bem [:cell :row :margin-top-small])}
      start-finalisation-button]
     (when (= status :finalisation-unsuccessful)
       [:div
        {:class (u/bem [:cell :row :padding-top-small])}
        [:div
         {:class (u/bem [:icon :warning :font-size-large])}]
        [:div
         {:class (u/bem [:text :padding-left-xx-small])}
         "That magic phrase doesn't look right"]])]))


(defn authorisation [properties views behaviours]
  (let [!status (re-frame/subscribe [:authorisation/status])
        !email-address-update-disabled? (re-frame/subscribe [:authorisation/email-address-update-disabled?])
        !email-address (re-frame/subscribe [:authorisation/email-address])
        !initialisation-disabled? (re-frame/subscribe [:authorisation/initialisation-disabled?])
        !initialisation-pending? (re-frame/subscribe [:authorisation/initialisation-pending?])
        !phrase-update-disabled? (re-frame/subscribe [:authorisation/phrase-update-disabled?])
        !phrase (re-frame/subscribe [:authorisation/phrase])
        !finalisation-disabled? (re-frame/subscribe [:authorisation/finalisation-disabled?])
        !finalisation-pending? (re-frame/subscribe [:authorisation/finalisation-pending?])]
    (fn [properties views behaviours]
      [view
       {:status @!status}
       {:email-address-input [input/input
                              {:placeholder "jane@smith.com"
                               :icon :envelope
                               :value @!email-address
                               :disabled? @!email-address-update-disabled?}
                              {}
                              {:on-change #(re-frame/dispatch [:authorisation/update-email-address %])}]
        :start-initialisation-button [button/button
                                      {:type :primary
                                       :label "Continue"
                                       :icon :arrow-right
                                       :disabled? @!initialisation-disabled?
                                       :pending? @!initialisation-pending?}
                                      {}
                                      {:on-click #(re-frame/dispatch [:authorisation/start-initialisation])}]
        :phrase-input [input/input
                       {:placeholder "donkey-purple-kettle"
                        :icon :magic-wand
                        :value @!phrase
                        :disabled? @!phrase-update-disabled?}
                       {}
                       {:on-change #(re-frame/dispatch [:authorisation/update-phrase %])}]
        :start-finalisation-button [button/button
                                    {:type :primary
                                     :label "Sign in"
                                     :icon :arrow-right
                                     :disabled? @!finalisation-disabled?
                                     :pending? @!finalisation-pending?}
                                    {}
                                    {:on-click #(re-frame/dispatch [:authorisation/start-finalisation])}]}
       {}])))
