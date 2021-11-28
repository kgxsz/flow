(ns flow.views.user-deletion
  (:require [re-frame.core :as re-frame]
            [flow.views.button :as button]
            [flow.utils :as u]))


(defn view [{}
            {:keys [start-button]}
            {}]
  [:div
   {:class (u/bem [:user-deletion])}
   start-button])


(defn user-deletion [{:keys [key user/id] :as properties} views behaviours]
  (let [!disabled? (re-frame/subscribe [:user-deletion/disabled? id])
        !pending? (re-frame/subscribe [:user-deletion/pending? key])]
    (fn [properties views behaviours]
      [view
       {}
       {:start-button [button/button
                       {:type :tertiary
                        :label "Delete"
                        :icon :trash
                        :disabled? @!disabled?
                        :pending? @!pending?}
                       {}
                       {:on-click #(re-frame/dispatch [:user-deletion/start key id])}]}
       {}])))
