(ns flow.views.widgets.toggle
  (:require [re-frame.core :as re-frame]
            [flow.utils :as u]))


(defn view [{:keys [label
                    value
                    disabled?]}
            _
            {:keys [on-toggle]}]
  [:div
   {:class (u/bem [:toggle]
                  [:cell :row :justify-start])}
   [:div
    {:class (u/bem [:toggle__body (when value :active)])
     :on-click (when-not disabled? on-toggle)}
    [:div
     {:class (u/bem [:toggle__body__knob (when value :active)])}]]
   [:div
    {:class (u/bem [:text :padding-left-xx-small])}
    label]])


(defn toggle [properties views behaviours]
  [view
   properties
   views
   behaviours])
