(ns flow.views.user-addition
  (:require [re-frame.core :as re-frame]
            [flow.views.widgets.input :as input]
            [flow.views.widgets.button :as button]
            [flow.views.widgets.toggle :as toggle]
            [flow.utils :as u]))


(defn view [{:keys [status]}
            {:keys [email-address-input
                    name-input
                    admin-role-toggle
                    start-button]}
            _]

  [:div
   {:class (u/bem [:user-addition])}
   [:div
    {:class (u/bem [:text :align-center :padding-top-medium])}
    "Add a new user"]
   name-input
   email-address-input
   [:div
    {:class (u/bem [:cell :margin-top-small])}
    admin-role-toggle]
   [:div
    {:class (u/bem [:cell :margin-top-small])}
    start-button]
   (when (= status :unsuccessful)
     [:div
      {:class (u/bem [:cell :row :padding-top-small])}
      [:div
       {:class (u/bem [:icon :warning :font-size-large])}]
      [:div
       {:class (u/bem [:text :padding-left-xx-small])}
       "The user could not be added"]])
   (when (= status :successful)
     [:div
      {:class (u/bem [:cell :row :padding-top-small])}
      [:div
       {:class (u/bem [:icon :checkmark-circle :font-size-large])}]
      [:div
       {:class (u/bem [:text :padding-left-xx-small])}
       "The user was added successfully"]])])


(defn user-addition [properties views behaviours]
  (let [!status (re-frame/subscribe [:user-addition/status])
        !name (re-frame/subscribe [:user-addition/name])
        !email-address (re-frame/subscribe [:user-addition/email-address])
        !admin-role? (re-frame/subscribe [:user-addition/admin-role?])
        !disabled? (re-frame/subscribe [:user-addition/disabled?])
        !pending? (re-frame/subscribe [:user-addition/pending?])]
    (fn [properties views behaviours]
      [view
       {:status @!status}
       {:name-input [input/input
                     {:placeholder "Jane"
                      :icon :user
                      :value @!name
                      :disabled? @!pending?}
                     {}
                     {:on-change #(re-frame/dispatch [:user-addition/update-name %])}]
        :email-address-input [input/input
                              {:placeholder "jane@smith.com"
                               :icon :envelope
                               :value @!email-address
                               :disabled? @!pending?}
                              {}
                              {:on-change #(re-frame/dispatch [:user-addition/update-email-address %])}]
        :admin-role-toggle [toggle/toggle
                            {:label "Admin"
                             :value @!admin-role?
                             :disabled? @!pending?}
                            {}
                            {:on-toggle #(re-frame/dispatch [:user-addition/toggle-admin-role])}]
        :start-button [button/button
                       {:type :primary
                        :label "Add user"
                        :icon :arrow-right
                        :disabled? @!disabled?
                        :pending? @!pending?}
                       {}
                       {:on-click #(re-frame/dispatch [:user-addition/start])}]}
       {}])))
