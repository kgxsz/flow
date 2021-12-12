(ns flow.views.pages.admin.users
  (:require [re-frame.core :as re-frame]
            [flow.views.widgets.link :as link]
            [flow.views.processes.user-addition :as user-addition]
            [flow.views.processes.pagination :as pagination]
            [flow.utils :as u]
            [cljs-time.coerce :as t.coerce]
            [cljs-time.format :as t.format]))


(defn view [{:keys [admin?]}
            {:keys [route-to-home-link
                    user-addition
                    pagination]}
            _]
  [:div
   {:class (u/bem [:page])}
   [:div
    {:class (u/bem [:page__body])}

    (if admin?

      [:div
       {:class (u/bem [:cell :column :padding-top-huge])}
       [:div
        {:class (u/bem [:cell :column])}
        [:div
         {:class (u/bem [:text :font-size-xx-huge])}
         "Users"]
        [:div
         {:class (u/bem [:cell :column :padding-top-large])}
         route-to-home-link]]
       [:div
        {:class (u/bem [:cell :width-xxx-huge :height-xx-tiny :margin-top-large :colour-grey-four])}]
       [:div
        {:class (u/bem [:cell :column :align-start])}
        user-addition]
       [:div
        {:class (u/bem [:cell :width-xxx-huge :height-xx-tiny :margin-top-large :colour-grey-four])}]
       [:div
        {:class (u/bem [:cell :column :align-start :padding-top-medium])}
        pagination]]

      [:div
       {:class (u/bem [:cell :column :padding-top-huge])}
       [:div
        {:class (u/bem [:icon :construction :font-size-xxx-huge])}]
       [:div
        {:class (u/bem [:cell :padding-top-xx-large])}
        [:div
         {:class (u/bem [:cell :column])}
         [:div
          {:class (u/bem [:text :font-size-xx-large])}
          "You're not supposed to be here"]
         [:div
          {:class (u/bem [:cell :column :padding-top-large])}
          route-to-home-link]]]])]

   [:div
    {:class (u/bem [:page__footer])}]])


(defn page [properties views behaviours]
  [view
   properties
   {:route-to-home-link [link/link
                         {:label "Home"}
                         {}
                         {:on-click #(re-frame/dispatch [:app/request-route-change :home])}]
    :user-addition [user-addition/user-addition
                    {:key [:views :user-addition]}
                    {}
                    {}]
    :pagination [pagination/pagination
                 {:key [:views :pagination]
                  :entity-type :users}
                 {}
                 {}]}
   {}])
