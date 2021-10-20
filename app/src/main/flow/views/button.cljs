(ns flow.views.button
  (:require [re-frame.core :as re-frame]
            [flow.utils :as u]))


(defn view [{:keys [type
                    label
                    icon
                    disabled?
                    pending?]}
            _
            {:keys [on-click]}]
  [:div
   {:class (u/bem [:button type (when disabled? :disabled) (when pending? :pending)]
                  [:cell :row :width-cover :height-x-large])
    :on-click (when-not (or disabled? pending?) on-click)}
   [:div
    {:class (u/bem [:text])}
    label]
   [:div
    {:class (u/bem [:cell :row :width-large :height-large])}
    (if pending?
      [:div
       {:class (u/bem [:button__spinner])}]
      [:div
       {:class (u/bem [:icon icon])}])]])


(defn button [properties views behaviours]
  [view
   properties
   {}
   behaviours])


