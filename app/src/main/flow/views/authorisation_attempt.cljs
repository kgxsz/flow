(ns flow.views.authorisation-attempt
  (:require [re-frame.core :as re-frame]
            [flow.views.input :as input]
            [flow.views.button :as button]
            [flow.utils :as u]))


(defn view [{:keys [status]}
            {:keys [email-address-input
                    start-initialisation-button
                    phrase-input
                    start-finalisation-button]}
            _]

  (case status

    (:idle :initialising)
    [:div
     {:key status
      :class (u/bem [:authorisation-attempt])}
     [:div
      {:class (u/bem [:text :align-center :padding-top-medium])}
      "Sign in with your email address"]
     email-address-input
     [:div
      {:class (u/bem [:cell :row :margin-top-small])}
      start-initialisation-button]]

    (:initialised :finalising :finalised-successfully :finalised-unsuccessfully)
    [:div
     {:key status
      :class (u/bem [:authorisation-attempt])}
     [:div
      {:class (u/bem [:text :align-center :padding-top-medium])}
      "We've emailed you a magic phrase"]
     phrase-input
     [:div
      {:class (u/bem [:cell :row :margin-top-small])}
      start-finalisation-button]
     (when (= status :finalised-unsuccessfully)
       [:div
        {:class (u/bem [:cell :row :padding-top-small])}
        [:div
         {:class (u/bem [:icon :font-size-medium :warning])}]
        [:div
         {:class (u/bem [:text :font-size-small :padding-left-tiny])}
         "That magic phrase doesn't look right"]])]))


(defn authorisation-attempt [properties views behaviours]
  (let [!status (re-frame/subscribe [:authorisation-attempt/status])
        !email-address-update-disabled? (re-frame/subscribe [:authorisation-attempt/email-address-update-disabled?])
        !email-address (re-frame/subscribe [:authorisation-attempt/email-address])
        !initialisation-disabled? (re-frame/subscribe [:authorisation-attempt/initialisation-disabled?])
        !initialising? (re-frame/subscribe [:authorisation-attempt/initialising?])
        !phrase-update-disabled? (re-frame/subscribe [:authorisation-attempt/phrase-update-disabled?])
        !phrase (re-frame/subscribe [:authorisation-attempt/phrase])
        !finalisation-disabled? (re-frame/subscribe [:authorisation-attempt/finalisation-disabled?])
        !finalising? (re-frame/subscribe [:authorisation-attempt/finalising?])]
    (fn [properties views behaviours]
      [view
       {:status @!status}
       {:email-address-input [input/input
                              {:placeholder "jane@smith.com"
                               :icon :envelope
                               :value @!email-address
                               :disabled? @!email-address-update-disabled?}
                              {}
                              {:on-change #(re-frame/dispatch [:authorisation-attempt/update-email-address %])}]
        :start-initialisation-button [button/button
                                      {:type :primary
                                       :label "Continue"
                                       :icon :arrow-right
                                       :disabled? @!initialisation-disabled?
                                       :pending? @!initialising?}
                                      {}
                                      {:on-click #(re-frame/dispatch [:authorisation-attempt/start-initialisation])}]
        :phrase-input [input/input
                       {:key [:views :app :views :pages.home :views :authorisation-attempt :views :phrase-input]
                        :placeholder "donkey-purple-kettle"
                        :icon :magic-wand
                        :value @!phrase
                        :disabled? @!phrase-update-disabled?}
                       {}
                       {:on-change #(re-frame/dispatch [:authorisation-attempt/update-phrase %])}]
        :start-finalisation-button [button/button
                                    {:type :primary
                                     :label "Sign in"
                                     :icon :arrow-right
                                     :disabled? @!finalisation-disabled?
                                     :pending? @!finalising?}
                                    {}
                                    {:on-click #(re-frame/dispatch [:authorisation-attempt/start-finalisation])}]}
       {}])))
