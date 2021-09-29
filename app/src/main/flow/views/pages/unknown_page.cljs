(ns flow.views.pages.unknown-page
  (:require [re-frame.core :as re-frame]
            [flow.utils :as u]))


(defn view [_ _ {:keys [request-route-update]}]
  [:div
   {:class (u/bem [:page])}
   [:div
    {:class (u/bem [:page__body])}
    [:div
     {:class (u/bem [:cell :column :padding-top-huge])}
     [:div
      {:class (u/bem [:icon :construction :font-size-xxx-huge :align-center])}]
     [:div
      {:class (u/bem [:text :font-size-x-large :padding-top-medium])
       :on-click request-route-update}
      "This page doesn't exist"]]]
   [:div
    {:class (u/bem [:page__footer])}]])


(defn unknown-page []
  [view
   {}
   {}
   {:request-route-update #(re-frame/dispatch [:unknown-page/route-update-requested])}])
