(ns flow.views.processes.deauthorisation
  (:require [re-frame.core :as re-frame]
            [flow.views.widgets.button :as button]
            [flow.utils :as u]))


(defn view [{}
            {:keys [start-button]}
            {}]

  [:div
   {:class (u/bem [:deauthorisation])}
   [:div
    {:class (u/bem [:cell :row :margin-top-small])}
    start-button]])


(defn deauthorisation [{:keys [key] :as properties} views behaviours]
  (let [!pending? (re-frame/subscribe [:deauthorisation/pending? key])]
    (fn [properties views behaviours]
      [view
       {}
       {:start-button [button/button
                       {:type :tertiary
                        :label "Sign out"
                        :icon :exit
                        :disabled? false
                        :pending? @!pending?}
                       {}
                       {:on-click #(re-frame/dispatch [:deauthorisation/start key])}]}
       {}])))
