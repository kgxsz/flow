(ns flow.views.widgets.toggle
  (:require [re-frame.core :as re-frame]
            [flow.utils :as u]))


(defn view [{:keys [value
                    label]}
            _
            {:keys [on-toggle]}]
  [:div
   {:class (u/bem [:toggle]
                  [:cell :row :justify-start])}
   [:div
    {:class (u/bem [:toggle__body (when value :active)])
     :on-click on-toggle}
    [:div
     {:class (u/bem [:toggle__body__knob (when value :active)])}]]
   [:div
    {:class (u/bem [:text :padding-left-xx-small])}
    label]])


(defn toggle [properties behaviours]
  (let [!value (re-frame/subscribe [(get-in properties [:subscriptions :value])])]
    (fn [properties behaviours]
      [view
       (assoc properties
              :value @!value)
       {}
       behaviours])))
