(ns flow.views.pages.admin.authorisations
  (:require [re-frame.core :as re-frame]
            [flow.views.link :as link]
            [flow.views.pager :as pager]
            [flow.views.authorisation :as authorisation]
            [flow.utils :as u]
            [cljs-time.coerce :as t.coerce]
            [cljs-time.format :as t.format]))


(defn view [{:keys [admin?]}
            {:keys [route-to-home-link
                    authorisations
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
         "Authorisations"]
        [:div
         {:class (u/bem [:cell :column :padding-top-large])}
         route-to-home-link]]
       [:div
        {:class (u/bem [:cell :margin-top-large :width-cover :height-xxx-tiny :colour-grey-four])}]
       [:div
        {:class (u/bem [:cell :column :align-start :padding-top-medium])}
        authorisations]
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
  (let [!ids (re-frame/subscribe [:pages.admin.authorisations/ids])
        !paging-exhausted? (re-frame/subscribe [:pages.admin.authorisations/paging-exhausted?])
        !paging-pending? (re-frame/subscribe [:pages.admin.authorisations/paging-pending?])]
    (fn [properties views behaviours]
      [view
       properties
       {:route-to-home-link [link/link
                             {:label "Go home"}
                             {}
                             {:on-click #(re-frame/dispatch [:app/route :home])}]
        :authorisations (for [id @!ids]
                          ^{:key id}
                          [authorisation/authorisation
                           {:authorisation/id id}
                           {}
                           {}])
        :pager [pager/pager
                {:exhausted? @!paging-exhausted?
                 :pending? @!paging-pending?}
                {}
                {:on-click #(re-frame/dispatch [:pages.admin.authorisations/start-paging])}]}
       {}])))
