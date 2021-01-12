(ns flow.views.home-page
  (:require [re-frame.core :as re-frame]
            [flow.utils :as u]))


(defn view [{:keys [authorised?]}
            _
            {:keys [authorise deauthorise update-route]}]
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
      "Home"]
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
        {:class (u/bem [:cell :row :padding-top-large])}
        [:div
         {:class (u/bem [:icon :arrow-right-circle :font-size-small])}]
        [:div
         {:class (u/bem [:text :font-size-large :padding-left-tiny])
          :on-click authorise}
         "Authorise"]])]]
   [:div
    {:class (u/bem [:page__footer])}]])


(defn home-page []
  (let [!authorised? (re-frame/subscribe [:authorised?])]
    (fn []
      [view
       {:authorised? @!authorised?}
       {}
       {:authorise #(re-frame/dispatch [:authorise])
        :deauthorise #(re-frame/dispatch [:deauthorise])
        :update-route #(re-frame/dispatch [:update-route :admin])}])))
