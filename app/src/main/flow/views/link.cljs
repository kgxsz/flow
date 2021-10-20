(ns flow.views.link
  (:require [re-frame.core :as re-frame]
            [flow.utils :as u]))


(defn view [{:keys [label]}
            _
            {:keys [on-click]}]
  [:div
   {:class (u/bem [:link]
                  [:cell :row])
    :on-click on-click}
   [:div
    {:class (u/bem [:cell :row :width-medium :height-medium])}
    [:div
     {:class (u/bem [:icon :arrow-right-circle :font-size-small])}]]
   [:div
    {:class (u/bem [:text])}
    label]])


(defn link [properties views behaviours]
  [view
   properties
   views
   behaviours])
