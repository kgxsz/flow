(ns flow.views.pages.admin.users
  (:require [re-frame.core :as re-frame]
            [flow.views.link :as link]
            [flow.utils :as u]
            [cljs-time.coerce :as t.coerce]
            [cljs-time.format :as t.format]))


(defn view [{:keys [authorised?
                    users]}
            {:keys [route-to-home-link]}
            _]
  [:div
   {:class (u/bem [:page])}
   [:div
    {:class (u/bem [:page__body])}

    (if authorised?

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
        {:class (u/bem [:cell :column :align-start :padding-top-medium])}
        (doall
         (for [user users]
           [:div
            {:key (:user/id user)
             :class (u/bem [:cell :column :align-start :padding-top-small])}
            [:div
             {:class (u/bem [:text :font-size-medium :font-weight-bold :padding-left-tiny])}
             (str (:user/id user))]
            [:div
             {:class (u/bem [:text :font-size-x-small :padding-left-tiny])}
             (str (:user/name user))]
            [:div
             {:class (u/bem [:text :font-size-x-small :padding-left-tiny])}
             (str (:user/email-address user))]
            [:div
             {:class (u/bem [:text :font-size-x-small :padding-left-tiny])}
             (->> (:user/created-at user)
                  (t.coerce/from-date)
                  (t.format/unparse (t.format/formatter "MMM dd, yyyy - HH:mm.ss")))]
            [:div
             {:class (u/bem [:text :font-size-x-small :padding-left-tiny])}
             (or
              (some->> (:user/deleted-at user)
                       (t.coerce/from-date)
                       (t.format/unparse (t.format/formatter "MMM dd, yyyy - HH:mm.ss")))
              "n/a")]
            [:div
             {:class (u/bem [:text :font-size-x-small :padding-left-tiny])}
             (->> (:user/roles user)
                  (map name)
                  (interpose ", ")
                  (apply str))]]))]]

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
  (let [!users (re-frame/subscribe [:pages.admin.users/users])]
    (fn [properties views behaviours]
      [view
       (assoc properties
              :users @!users)
       {:route-to-home-link [link/link
                             {:label "Go home"}
                             {}
                             {:on-click #(re-frame/dispatch [:app/route :home])}]}
       {}])))
