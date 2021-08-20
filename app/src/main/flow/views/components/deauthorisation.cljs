(ns flow.views.components.deauthorisation
  (:require [re-frame.core :as re-frame]
            [flow.views.widgets.button :as button]
            [flow.utils :as u]))


(defn view [{:keys [status]}
            {:keys [button]}
            {:keys [start]}]

  [:div
   {:class (u/bem [:deauthorisation])}
   [:div
    {:class (u/bem [:cell :row :margin-top-small])}
    [button
     {:subscriptions {:disabled? :deauthorisation/disabled?
                      :pending? :deauthorisation/pending?}
      :label "Sign out"
      :icon :exit}
     {:on-click start}]]
   (when (= status :error)
     [:div
      {:class (u/bem [:cell :row :padding-top-small])}
      [:div
       {:class (u/bem [:icon :font-size-medium :warning])}]
      [:div
       {:class (u/bem [:text :font-size-small :padding-left-tiny])}
       ;; TODO - make this better
       "Looks like we're having trouble here."]])])


(defn deauthorisation []
  (let [!status (re-frame/subscribe [:deauthorisation/status])]
    (fn []
      [view
       {:status @!status}
       {:button button/primary-button}
       {:start #(re-frame/dispatch [:deauthorisation/started])}])))
