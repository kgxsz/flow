(ns flow.views.widgets.link
  (:require [re-frame.core :as re-frame]
            [flow.utils :as u]))


(defn view [{:keys [label
                    icon
                    pending?]}
            _
            {:keys [on-click]}]
  [:div
   {:class (u/bem [:link (when pending? :pending)]
                  [:cell :row])
    :on-click (when-not pending? on-click)}
   [:div
    {:class (u/bem [:cell :row :width-medium :height-medium])}
    (if pending?
      [:div
       {:class (u/bem [:link__spinner])}]
      [:div
       {:class (u/bem [:icon :arrow-right-circle :font-size-small])}])]
   [:div
    {:class (u/bem [:text])}
    label]])


(defn link [properties behaviours]
  (let [!pending? (re-frame/subscribe [(get-in properties [:subscriptions :pending?])])]
    (fn [properties behaviours]
      [view
       (assoc properties :pending? @!pending?)
       {}
       behaviours])))
