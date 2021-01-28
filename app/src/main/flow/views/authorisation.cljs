(ns flow.views.authorisation
  (:require [re-frame.core :as re-frame]
            [flow.views.input :as input]
            [flow.utils :as u]))


(defn view [{:keys [input-value]}
            _
            {:keys [update-input-value
                    initialise-authorisation
                    finalise-authorisation]}]
  [:div
   {:class (u/bem [:authorisation])}
   [:div
    {:class (u/bem [:text :align-center :padding-top-medium])}
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
     {:class (u/bem [:icon :arrow-right :colour-white-one :padding-left-tiny])}]]])



(defn authorisation []
  (let [!input-value (re-frame/subscribe [:input-value])]
    (fn []
      [view
       {:input-value @!input-value}
       {:email-input}
       {:update-input-value #(re-frame/dispatch [:update-input-value (.. % -target -value)])
        :initialise-authorisation #(re-frame/dispatch [:initialise-authorisation %])
        :finalise-authorisation #(re-frame/dispatch [:finalise-authorisation %])}])))
