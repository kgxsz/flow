(ns flow.views.pages.unknown-page
  (:require [re-frame.core :as re-frame]
            [flow.utils :as u]))


(defn view [_ _ {:keys [update-route]}]
  [:div
   {:class (u/bem [:page])}
   [:div
    {:class (u/bem [:page__body])}
    [:div
     {:class (u/bem [:cell :column :padding-top-huge])}
     [:div
      {:class (u/bem [:icon :construction :font-size-xx-huge :align-center])}]
     [:div
      {:class (u/bem [:text :font-size-xx-huge :align-center])
       :on-click update-route}
      "Unknown"]]]
   [:div
    {:class (u/bem [:page__footer])}]])


(defn unknown-page []
  [view
   {}
   {}
   {:update-route #(re-frame/dispatch [:update-route :home])}])
