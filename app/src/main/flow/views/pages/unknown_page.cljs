(ns flow.views.pages.unknown-page
  (:require [re-frame.core :as re-frame]
            [flow.utils :as u]))


(defn view [{:keys [status]}
            _
            {:keys [route-home]}]
  [:div
   {:class (u/bem [:page])}
   [:div
    {:class (u/bem [:page__body])}
    (case status
      :uninitialised
      [:div
       {:class (u/bem [:text :align-center :padding-top-medium])}
       ;; TODO - deal with this more nicely
       "********************HSKJSFHLJHFJKSHFLKJYe!!!!!!!!!!!!!!!!!!"]

      :initialised
      [:div
       {:class (u/bem [:cell :column :padding-top-huge])}
       [:div
        {:class (u/bem [:icon :construction :font-size-xx-huge :align-center])}]
       [:div
        {:class (u/bem [:text :font-size-xx-huge :align-center])
         :on-click route-home}
        "Unknown"]])]
   [:div
    {:class (u/bem [:page__footer])}]])


(defn unknown-page []
  (let [!status (re-frame/subscribe [:unknown-page/status])]
    (fn []
      [view
       {:status @!status}
       {}
       {:route-home #(re-frame/dispatch [:unknown-page/route-home])}])))
