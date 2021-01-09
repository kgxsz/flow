(ns flow.views.admin-page
  (:require [re-frame.core :as re-frame]
            [flow.utils :as u]))


(defn view [_ {:keys [authorise deauthorise find]}]
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
      "Admin"]
     [:div
      {:class (u/bem [:cell :row :padding-top-large])}
      [:div
       {:class (u/bem [:icon :arrow-right-circle :font-size-small])}]
      [:div
       {:class (u/bem [:text :font-size-large :padding-left-tiny])
        :on-click authorise}
       "authorise"]]
     [:div
      {:class (u/bem [:cell :row :padding-top-tiny])}
      [:div
       {:class (u/bem [:icon :arrow-right-circle :font-size-small])}]
      [:div
       {:class (u/bem [:text :font-size-large :padding-left-tiny])
        :on-click deauthorise}
       "deauthorise"]]
     [:div
      {:class (u/bem [:cell :row :padding-top-tiny])}
      [:div
       {:class (u/bem [:icon :arrow-right-circle :font-size-small])}]
      [:div
       {:class (u/bem [:text :font-size-large :padding-left-tiny])
        :on-click find}
       "find"]]]]
   [:div
    {:class (u/bem [:page__footer])}]])


(defn admin-page []
  [view
   {}
   {:authorise #(re-frame/dispatch [:authorise])
    :deauthorise #(re-frame/dispatch [:deauthorise])
    :find #(re-frame/dispatch [:find])}])
