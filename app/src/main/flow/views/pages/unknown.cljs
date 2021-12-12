(ns flow.views.pages.unknown
  (:require [re-frame.core :as re-frame]
            [flow.views.widgets.link :as link]
            [flow.utils :as u]))


(defn view [_
            {:keys [route-to-home-link]}
            _]
  [:div
   {:class (u/bem [:page])}
   [:div
    {:class (u/bem [:page__body])}
    [:div
     {:class (u/bem [:cell :column :padding-top-huge])}
     [:div
      {:class (u/bem [:icon :construction :font-size-xxx-huge :align-center])}]
     [:div
      {:class (u/bem [:text :font-size-xx-large :padding-top-medium])}
      "This page doesn't exist"]
     [:div
      {:class (u/bem [:cell :margin-top-large])}
      route-to-home-link]]]
   [:div
    {:class (u/bem [:page__footer])}]])


(defn page [properties views behaviours]
  [view
   {}
   {:route-to-home-link [link/link
                         {:label "Home"}
                         {}
                         {:on-click #(re-frame/dispatch [:app/request-route-change :home])}]}
   {}])
