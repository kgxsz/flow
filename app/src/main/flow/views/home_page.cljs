(ns flow.views.home-page
  (:require [re-frame.core :as re-frame]
            [flow.views.authorisation :as authorisation]
            [flow.utils :as u]))


(defn view [{:keys [authorised?]}
            {:keys [authorisation]}
            {:keys [deauthorise]}]
  [:div
   {:class (u/bem [:page])}
   [:div
    {:class (u/bem [:page__body])}
    [:div
     {:class (u/bem [:cell :column :padding-top-huge])}
     [:div
      {:class (u/bem [:icon :leaf :font-size-xxx-huge])}]
     (if authorised?
       [:div
        {:class (u/bem [:cell :row :padding-top-large])}
        [:div
         {:class (u/bem [:icon :arrow-right-circle :font-size-small])}]
        [:div
         {:class (u/bem [:text :font-size-large :padding-left-tiny])
          :on-click deauthorise}
         "Deauthorise"]]
       [:div
        {:class (u/bem [:cell :padding-top-xx-large])}
        [authorisation]])]]
   [:div
    {:class (u/bem [:page__footer])}]])


(defn home-page []
  (let [!authorised? (re-frame/subscribe [:authorised?])]
    (fn []
      [view
       {:authorised? @!authorised?}
       {:authorisation authorisation/authorisation}
       {:deauthorise #(re-frame/dispatch [:deauthorise])}])))
