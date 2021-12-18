(ns flow.views.widgets.button
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
                  [:cell :row])
    :on-click (when-not (or disabled? pending?) on-click)}

   (when (contains? #{:primary :secondary} type)
     [:div
      {:class (u/bem [:button__label type]
                     [:text :padding-right-xxx-small])}
      label])

   [:div
    {:class (u/bem [:cell :row :height-large :width-small])}
    (if pending?
      [:div
       {:class (u/bem [:button__spinner type])}]
      [:div
       {:class (u/bem [:icon icon :font-size-medium]
                      [:button__icon type])}])]

   (when (= :tertiary type)
     [:div
      {:class (u/bem [:button__label type]
                     [:text :padding-left-xxx-small])}
      label])])


(defn button [properties views behaviours]
  [view
   properties
   views
   behaviours])

