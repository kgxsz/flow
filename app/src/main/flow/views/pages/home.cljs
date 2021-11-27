(ns flow.views.pages.home
  (:require [re-frame.core :as re-frame]
            [flow.views.authorisation-attempt :as authorisation-attempt]
            [flow.views.deauthorisation :as deauthorisation]
            [flow.views.button :as button]
            [flow.views.link :as link]
            [flow.utils :as u]))


(defn view [{:keys [status
                    authorised?
                    current-user]}
            {:keys [authorisation-attempt
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
      {:class (u/bem [:cell :padding-top-xx-large])}

      (if authorised?
        [:div
         {:class (u/bem [:cell :column])}
         [:div
          {:class (u/bem [:text :font-size-xx-huge])}
          (str "Hi " (:user/name current-user))]
         [:div
          {:class (u/bem [:cell :column :padding-top-large])}
          route-to-users-link
          route-to-authorisations-link
          deauthorisation]]

        authorisation-attempt)]]]

   [:div
    {:class (u/bem [:page__footer])}]])


(defn page [properties views behaviours]
  [view
   properties
   {:authorisation-attempt [authorisation-attempt/authorisation-attempt
                            {}
                            {}
                            {}]
    :route-to-users-link [link/link
                          {:label "See users"}
                          {}
                          {:on-click #(re-frame/dispatch [:app/route :admin.users])}]
    :route-to-authorisations-link [link/link
                                   {:label "See authorisations"}
                                   {}
                                   {:on-click #(re-frame/dispatch [:app/route :admin.authorisations])}]
    :deauthorisation [deauthorisation/deauthorisation
                      {}
                      {}
                      {}]}
   {}])
