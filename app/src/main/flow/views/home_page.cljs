(ns flow.views.home-page
  (:require [re-frame.core :as re-frame]
            [flow.views.authorisation :as authorisation]
            [flow.views.navigation :as navigation]
            [flow.utils :as u]))


(defn view [{:keys [authorised?]}
            {:keys [navigation
                    authorisation]}
            {:keys [deauthorise]}]
  [:div
   {:class (u/bem [:page])}
   [:div
    {:class (u/bem [:page__body])}
    [:div
     {:class (u/bem [:cell :column :padding-top-huge])}
     [:div
      {:class (u/bem [:icon :leaf :font-size-xxx-huge])}]
     [:div
      {:class (u/bem [:cell :padding-top-xx-large])}
      (if authorised?
        [navigation]
        [authorisation])]]]
   [:div
    {:class (u/bem [:page__footer])}]])


(defn home-page []
  (let [!authorised? (re-frame/subscribe [:authorised?])]
    (fn []
      [view
       {:authorised? @!authorised?}
       {:navigation navigation/navigation
        :authorisation authorisation/authorisation}
       {}])))
