(ns flow.views.loading-page
  (:require [re-frame.core :as re-frame]
            [flow.utils :as u]))


(defn view []
  [:div
   {:class (u/bem [:page])}
   [:div
    {:class (u/bem [:page__body])}
    [:div
     {:class (u/bem [:cell :column :padding-top-huge])}
     [:div
      {:class (u/bem [:icon :construction :font-size-xx-huge :align-center])}]
     [:div
      {:class (u/bem [:text :font-size-xx-huge :align-center])}
      "Loading"]]]
   [:div
    {:class (u/bem [:page__footer])}]])


(defn loading-page []
  [view])
