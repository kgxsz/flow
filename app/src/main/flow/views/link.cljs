(ns flow.views.link
  (:require [re-frame.core :as re-frame]
            [flow.utils :as u]))


(defn view [{:keys [label]}
            _
            {:keys [on-click]}]
  [:div
   {:class (u/bem [:link])
    :on-click on-click}
   [:div
    {:class (u/bem [:text])}
    label]])


(defn link [properties views behaviours]
  [view
   properties
   views
   behaviours])
