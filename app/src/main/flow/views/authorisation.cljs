(ns flow.views.authorisation
  (:require [re-frame.core :as re-frame]
            [flow.views.input :as input]
            [flow.views.button :as button]
            [flow.utils :as u]))


(defn view [{:keys [authorisation-initialised?
                    authorisation-failed?]}
            {:keys [input
                    button]}
            {:keys [update-authorisation-email-address
                    initialise-authorisation
                    update-authorisation-phrase
                    finalise-authorisation]}]
  (if authorisation-initialised?
    [:div
     {:key :authorisation-initialised
      :class (u/bem [:authorisation])}
     [:div
      {:class (u/bem [:text :align-center :padding-top-medium])}
      "We've sent you a magic phrase."]
     [input
      {:subscriptions {:value :authorisation-phrase}
       :placeholder "donkey-purple-kettle"
       :icon :magic-wand}
      {:on-change update-authorisation-phrase}]
     [:div
      {:class (u/bem [:cell :row :margin-top-small])}
      [button
       {:subscriptions {:disabled? :authorisation-finalisation-disabled?}
        :label "Sign in"
        :icon :arrow-right}
       {:on-click finalise-authorisation}]]
     (when authorisation-failed?
       [:div
        {:class (u/bem [:cell :row :padding-top-small])}
        [:div
         {:class (u/bem [:icon :font-size-medium :warning])}]
        [:div
         {:class (u/bem [:text :font-size-small :padding-left-tiny])}
         "That magic phrase doesn't look right."]])]

    [:div
     {:key :authorisation-uninitialised
      :class (u/bem [:authorisation])}
     [:div
      {:class (u/bem [:text :align-center :padding-top-medium])}
      "Sign in with your email address."]
     [input
      {:subscriptions {:value :authorisation-email-address}
       :placeholder "jane@smith.com"
       :icon :envelope}
      {:on-change update-authorisation-email-address}]
     [:div
      {:class (u/bem [:cell :row :margin-top-small])}
      [button
       {:subscriptions {:disabled? :authorisation-initialisation-disabled?}
        :label "Continue"
        :icon :arrow-right}
       {:on-click initialise-authorisation}]]]))


(defn authorisation []
  (let [!authorisation-initialised? (re-frame/subscribe [:authorisation-initialised?])
        !authorisation-failed? (re-frame/subscribe [:authorisation-failed?])]
    (fn []
      [view
       {:authorisation-initialised? @!authorisation-initialised?
        :authorisation-failed? @!authorisation-failed?}
       {:input input/input
        :button button/primary-button}
       {:update-authorisation-email-address
        #(re-frame/dispatch [:update-authorisation-email-address %])
        :initialise-authorisation
        #(re-frame/dispatch [:initialise-authorisation])
        :update-authorisation-phrase
        #(re-frame/dispatch [:update-authorisation-phrase %])
        :finalise-authorisation
        #(re-frame/dispatch [:finalise-authorisation])}])))
