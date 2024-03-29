(ns flow.views.pages.home
  (:require [re-frame.core :as re-frame]
            [flow.views.processes.authorisation :as authorisation]
            [flow.views.processes.deauthorisation :as deauthorisation]
            [flow.views.widgets.button :as button]
            [flow.views.widgets.link :as link]
            [flow.utils :as u]))


(defn view [{:keys [authorised?
                    current-user]}
            {:keys [authorisation
                    route-to-users-link
                    route-to-authorisations-link
                    deauthorisation]}
            _]
  [:div
   {:class (u/bem [:page])}
   [:div
    {:class (u/bem [:page__body])}

    [:div
     {:class (u/bem [:cell :column :padding-top-huge])}
     [:div
      {:class (u/bem [:icon :leaf :font-size-xxx-huge])}]
     [:div
      {:class (u/bem [:cell :padding-top-xx-large])}]]

    (if authorised?
      [:div
       {:class (u/bem [:cell :column])}
       [:div
        {:class (u/bem [:text :font-size-xx-huge])}
        (str "Hi " (:user/name current-user))]
       [:div
        {:class (u/bem [:cell :width-xxx-huge :height-xx-tiny :margin-top-large :colour-grey-four])}]
       [:div
        {:class (u/bem [:cell :column :padding-top-large])}
        [:div
         {:class (u/bem [:cell :padding-top-x-small])}
         route-to-users-link]
        [:div
         {:class (u/bem [:cell :padding-top-x-small])}
         route-to-authorisations-link]]
       [:div
        {:class (u/bem [:cell :padding-top-x-large])}
        deauthorisation]]

      authorisation)]

   [:div
    {:class (u/bem [:page__footer])}]])


(defn page [properties views behaviours]
  [view
   properties
   {:authorisation [authorisation/authorisation
                    {:key [:views :authorisation]}
                    {}
                    {}]
    :route-to-users-link [link/link
                          {:label "Users"}
                          {}
                          {:on-click #(re-frame/dispatch [:app/request-route-change :admin.users])}]
    :route-to-authorisations-link [link/link
                                   {:label "Authorisations"}
                                   {}
                                   {:on-click #(re-frame/dispatch [:app/request-route-change :admin.authorisations])}]
    :deauthorisation [deauthorisation/deauthorisation
                      {:key [:views :deauthorisation]}
                      {}
                      {}]}
   {}])
