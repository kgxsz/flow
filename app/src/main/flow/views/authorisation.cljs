(ns flow.views.authorisation
  (:require [re-frame.core :as re-frame]
            [flow.utils :as u]))


(defn view [{:keys [input-value]}
            _
            {:keys [update-input-value
                    initialise-authorisation
                    finalise-authorisation]}]
  [:div
   {:class (u/bem [:authorisation])}
   [:div
    {:class (u/bem [:text :font-size-xxx-huge])}
    "Hello."]
   [:div
    {:class (u/bem [:text :padding-top-medium])}
    "Let's start off with your email address."]
   [:div
    {:class (u/bem [:authorisation__email-address]
                   [:cell :padding-top-tiny])}
    [:div
     {:class (u/bem [:authorisation__email-address__icon]
                    [:icon :envelope :font-size-large :colour-black-four])}]
    [:input
     {:class (u/bem [:input]
                    [:authorisation__email-address__input])
      :type :text
      :value input-value
      :placeholder "jane@smith.com"
      :on-change update-input-value}]]
   [:div
    {:class (u/bem [:authorisation__email-address__button]
                   [:cell :row :margin-top-small])}
    [:div
     {:class (u/bem [:text :colour-white-one])}
     "Continue"]
    [:div
     {:class (u/bem [:icon :arrow-right :colour-white-one :padding-left-tiny])}]]
   [:div
    {:class (u/bem [:cell :row :justify-start :padding-top-large])}
    [:div
     {:class (u/bem [:icon :warning :font-size-x-small :colour-black-four])}]
    [:div
     {:class (u/bem [:text :font-size-x-small :padding-left-tiny :colour-grey-one])}
     "Why do we need your email address?"]]])



(defn authorisation []
  (let [!input-value (re-frame/subscribe [:input-value])]
    (fn []
      [view
       {:input-value @!input-value}
       {}
       {:update-input-value #(re-frame/dispatch [:update-input-value (.. % -target -value)])
        :initialise-authorisation #(re-frame/dispatch [:initialise-authorisation %])
        :finalise-authorisation #(re-frame/dispatch [:finalise-authorisation %])}])))
