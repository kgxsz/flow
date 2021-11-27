(ns flow.views.pages.admin.users
  (:require [re-frame.core :as re-frame]
            [flow.views.link :as link]
            [flow.views.pager :as pager]
            [flow.views.user :as user]
            [flow.views.user-addition :as user-addition]
            [flow.utils :as u]
            [cljs-time.coerce :as t.coerce]
            [cljs-time.format :as t.format]))


(defn view [{:keys [admin?]}
            {:keys [route-to-home-link
                    user-addition
                    users
                    pager]}
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
        {:class (u/bem [:cell :margin-top-large :width-cover :height-xxx-tiny :colour-grey-four])}]
       [:div
        {:class (u/bem [:cell :column :align-start])}
        user-addition]
       [:div
        {:class (u/bem [:cell :margin-top-xx-large :width-cover :height-xxx-tiny :colour-grey-four])}]
       [:div
        {:class (u/bem [:cell :column :align-start :padding-top-medium])}
        users]
       [:div
        {:class (u/bem [:cell :padding-top-medium])}
        pager]]

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
  (let [!ids (re-frame/subscribe [:pages.admin.users/ids])
        !paging-exhausted? (re-frame/subscribe [:pages.admin.users/paging-exhausted?])
        !paging-pending? (re-frame/subscribe [:pages.admin.users/paging-pending?])]
    (fn [properties views behaviours]
      [view
       properties
       {:route-to-home-link [link/link
                             {:label "Go home"}
                             {}
                             {:on-click #(re-frame/dispatch [:app/route :home])}]
        :user-addition [user-addition/user-addition
                        {}
                        {}
                        {}]
        :users (for [id @!ids]
                 ^{:key id}
                 [user/user
                  {:user/id id}
                  {}
                  {}])
        :pager [pager/pager
                {:exhausted? @!paging-exhausted?
                 :pending? @!paging-pending?}
                {}
                {:on-click #(re-frame/dispatch [:pages.admin.users/start-paging])}]}
       {}])))
