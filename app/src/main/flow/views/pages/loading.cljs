(ns flow.views.pages.loading
  (:require [re-frame.core :as re-frame]
            [flow.utils :as u]))


(defn view [_ _ _]
  [:div
   {:class (u/bem [:page])}
   [:div
    {:class (u/bem [:page__body])}
    [:div
     {:class (u/bem [:cell :column :padding-top-huge])}
     [:div
      {:class (u/bem [:icon :leaf :font-size-xxx-huge])}]]]
   [:div
    {:class (u/bem [:page__footer])}]])


(defn page [properties views behaviours]
  [view {} {} {}])
