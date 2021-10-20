(ns flow.views.authorisation-attempt
  (:require [re-frame.core :as re-frame]
            [flow.views.input :as input]
            [flow.views.button :as button]
            [flow.utils :as u]))


(defn view [{:keys [status]}
            {:keys [email-address-input
                    initialise-button
                    phrase-input
                    finalise-button]}
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
      initialise-button]]

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
      finalise-button]
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
        !phrase-update-disabled? (re-frame/subscribe [:authorisation-attempt/phrase-update-disabled?])
        !initialisation-disabled? (re-frame/subscribe [:authorisation-attempt/initialisation-disabled?])
        !finalisation-disabled? (re-frame/subscribe [:authorisation-attempt/finalisation-disabled?])]
    (fn [properties views behaviours]
      [view
       {:status @!status}
       {:email-address-input [input/input
                              {:key [:views :app :views :pages.home :views :authorisation-attempt :views :email-address-input]
                               :placeholder "jane@smith.com"
                               :icon :envelope
                               :disabled? @!email-address-update-disabled?}
                              {}
                              {}]
        :initialise-button [button/button
                            {:type :primary
                             :label "Continue"
                             :icon :arrow-right
                             :disabled? @!initialisation-disabled?
                             :pending? (= :initialising @!status)}
                            {}
                            {:on-click #(re-frame/dispatch [:authorisation-attempt/initialise])}]
        :phrase-input [input/input
                       {:key [:views :app :views :pages.home :views :authorisation-attempt :views :phrase-input]
                        :placeholder "donkey-purple-kettle"
                        :icon :magic-wand
                        :disabled? @!phrase-update-disabled?}
                       {}
                       {}]
        :finalise-button [button/button
                          {:type :primary
                           :label "Sign in"
                           :icon :arrow-right
                           :disabled? @!finalisation-disabled?
                           :pending? (= :finalising @!status)}
                          {}
                          {:on-click #(re-frame/dispatch [:authorisation-attempt/finalise])}]}
       {}])))
