(ns flow.views.admin-page
  (:require [re-frame.core :as re-frame]
            [flow.utils :as u]
            [flow.views.input :as input]
            [flow.views.toggle :as toggle]
            [flow.views.button :as button]
            [cljs-time.coerce :as t.coerce]
            [cljs-time.format :as t.format]))


(defn view [{:keys [content
                    authorised?
                    users
                    authorisations]}
            {:keys [input
                    toggle
                    button]}
            {:keys [update-route
                    update-user-addition-name
                    update-user-addition-email-address
                    toggle-user-addition-admin-role?
                    add-user
                    delete-user]}]
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

      (when authorised?
        [:div
         {:class (u/bem [:cell :column])}
         [:div
          {:class (u/bem [:text :font-size-xx-huge])}
          "Admin"]

         (case content
           :default
           [:div
            {:class (u/bem [:cell :column :padding-top-large])}
            [:div
             {:class (u/bem [:cell :row :padding-top-tiny])}
             [:div
              {:class (u/bem [:icon :arrow-right-circle :font-size-small])}]
             [:div
              {:class (u/bem [:text :font-size-large :padding-left-tiny])
               :on-click (partial update-route :admin.users)}
              "Users"]]
            [:div
             {:class (u/bem [:cell :row :padding-top-tiny])}
             [:div
              {:class (u/bem [:icon :arrow-right-circle :font-size-small])}]
             [:div
              {:class (u/bem [:text :font-size-large :padding-left-tiny])
               :on-click (partial update-route :admin.authorisations)}
              "Authorisations"]]]

           :users
           [:div
            {:class (u/bem [:cell :column])}
            [:div
             {:class (u/bem [:cell :width-cover])}
             [:div
              {:class (u/bem [:text :align-center :padding-top-medium])}
              "Add a user"]
             [input
              {:subscriptions {:value :user-addition-name}
               :placeholder "Jane"
               :icon :user}
              {:on-change update-user-addition-name}]
             [input
              {:subscriptions {:value :user-addition-email-address}
               :placeholder "jane@smith.com"
               :icon :envelope}
              {:on-change update-user-addition-email-address}]
             [:div
              {:class (u/bem [:cell :margin-top-small])}
              [toggle
               {:subscriptions {:value :user-addition-admin-role?}
                :label "Admin"}
               {:on-toggle toggle-user-addition-admin-role?}]]
             [:div
              {:class (u/bem [:cell :margin-top-small])}
              [button
               {:subscriptions {:disabled? :user-addition-disabled?}
                :label "Add user"
                :icon :arrow-right}
               {:on-click add-user}]]]
            (doall
             (for [user users]
               [:div
                {:key (:user/id user)
                 :class (u/bem [:cell :width-cover :padding-top-medium])}
                [:div
                 {:class (u/bem [:cell :row :justify-start])}
                 [:div
                  {:class (u/bem [:icon :user :font-size-small])}]
                 [:div
                  {:class (u/bem [:text :font-size-small :padding-left-tiny])}
                  (:user/name user)]]
                [:div
                 {:class (u/bem [:cell :row :justify-start])}
                 [:div
                  {:class (u/bem [:icon :license :font-size-small])}]
                 [:div
                  {:class (u/bem [:text :font-size-small :padding-left-tiny])}
                  (str (:user/id user))]]
                [:div
                 {:class (u/bem [:cell :row :justify-start])}
                 [:div
                  {:class (u/bem [:icon :envelope :font-size-small])}]
                 [:div
                  {:class (u/bem [:text :font-size-small :padding-left-tiny])}
                  (:user/email-address user)]]
                [:div
                 {:class (u/bem [:cell :row :justify-start])}
                 [:div
                  {:class (u/bem [:icon :enter-down :font-size-small])}]
                 [:div
                  {:class (u/bem [:text :font-size-small :padding-left-tiny])}
                  (->> (:user/created-at user)
                       (t.coerce/from-date)
                       (t.format/unparse (t.format/formatter "MMM dd, yyyy - HH:mm.ss")))]]
                [:div
                 {:class (u/bem [:cell :row :justify-start])}
                 [:div
                  {:class (u/bem [:icon :trash :font-size-small])
                   :on-click (partial delete-user (:user/id user))}]
                 [:div
                  {:class (u/bem [:text :font-size-small :padding-left-tiny])}
                  (or
                   (some->> (:user/deleted-at user)
                            (t.coerce/from-date)
                            (t.format/unparse (t.format/formatter "MMM dd, yyyy - HH:mm.ss")))
                   "n/a")]]
                [:div
                 {:class (u/bem [:cell :row :justify-start])}
                 [:div
                  {:class (u/bem [:icon :tag :font-size-small])}]
                 [:div
                  {:class (u/bem [:text :font-size-small :padding-left-tiny])}
                  (->> (:user/roles user)
                       (map name)
                       (interpose ", ")
                       (apply str))]]]))]

           :authorisations
           (doall
            (for [authorisation authorisations]
              [:div
               {:key (:authorisation/id authorisation)
                :class (u/bem [:cell :width-cover :padding-top-medium])}
               [:div
                {:class (u/bem [:cell :row :justify-start])}
                [:div
                 {:class (u/bem [:icon :license :font-size-small])}]
                [:div
                 {:class (u/bem [:text :font-size-small :padding-left-tiny])}
                 (str (:authorisation/id authorisation))]]
               [:div
                {:class (u/bem [:cell :row :justify-start])}
                [:div
                 {:class (u/bem [:icon :user :font-size-small])}]
                [:div
                 {:class (u/bem [:text :font-size-small :padding-left-tiny])}
                 (str (:user/id authorisation))]]
               [:div
                {:class (u/bem [:cell :row :justify-start])}
                [:div
                 {:class (u/bem [:icon :magic-wand :font-size-small])}]
                [:div
                 {:class (u/bem [:text :font-size-small :padding-left-tiny])}
                 (str (:authorisation/phrase authorisation))]]
               [:div
                {:class (u/bem [:cell :row :justify-start])}
                [:div
                 {:class (u/bem [:icon :enter-down :font-size-small])}]
                [:div
                 {:class (u/bem [:text :font-size-small :padding-left-tiny])}
                 (->> (:authorisation/created-at authorisation)
                      (t.coerce/from-date)
                      (t.format/unparse (t.format/formatter "MMM dd, yyyy - HH:mm.ss")))]]
               [:div
                {:class (u/bem [:cell :row :justify-start])}
                [:div
                 {:class (u/bem [:icon :checkmark-circle :font-size-small])}]
                [:div
                 {:class (u/bem [:text :font-size-small :padding-left-tiny])}
                 (or
                  (some->> (:authorisation/granted-at authorisation)
                           (t.coerce/from-date)
                           (t.format/unparse (t.format/formatter "MMM dd, yyyy - HH:mm.ss")))
                  "n/a")]]])))])

      [:div
       {:class (u/bem [:cell :row :padding-top-large])}
       [:div
        {:class (u/bem [:icon :arrow-right-circle :font-size-small])}]
       [:div
        {:class (u/bem [:text :font-size-large :padding-left-tiny])
         :on-click (partial update-route :home)}
        "Go home"]]]]]
   [:div
    {:class (u/bem [:page__footer])}]])


(defn admin-page [properties]
  (let [!authorised? (re-frame/subscribe [:authorised?])
        !users (re-frame/subscribe [:users])
        !authorisations (re-frame/subscribe [:authorisations])]
    (fn [properties]
      [view
       (assoc properties
              :authorised? @!authorised?
              :users @!users
              :authorisations @!authorisations)
       {:input input/input
        :toggle toggle/toggle
        :button button/primary-button}
       {:update-route
        #(re-frame/dispatch [:update-route %])
        :update-user-addition-name
        #(re-frame/dispatch [:update-user-addition-name %])
        :update-user-addition-email-address
        #(re-frame/dispatch [:update-user-addition-email-address %])
        :toggle-user-addition-admin-role?
        #(re-frame/dispatch [:toggle-user-addition-admin-role?])
        :add-user
        #(re-frame/dispatch [:add-user %])
        :delete-user
        #(re-frame/dispatch [:delete-user %])}])))
