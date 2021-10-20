(ns flow.views.pages.admin.authorisations
  (:require [re-frame.core :as re-frame]
            [flow.views.link :as link]
            [flow.utils :as u]
            [cljs-time.coerce :as t.coerce]
            [cljs-time.format :as t.format]))


(defn view [{:keys [authorised?
                    authorisations]}
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
         "Authorisations"]
        [:div
         {:class (u/bem [:cell :column :padding-top-large])}
         route-to-home-link]]
       [:div
        {:class (u/bem [:cell :column :align-start :padding-top-medium])}
        (doall
         (for [authorisation authorisations]
           [:div
            {:key (:authorisation/id authorisation)
             :class (u/bem [:cell :column :align-start :padding-top-small])}
            [:div
             {:class (u/bem [:text :font-size-x-medium :font-weight-bold :padding-left-tiny])}
             (str (:authorisation/id authorisation))]
            [:div
             {:class (u/bem [:text :font-size-x-small :padding-left-tiny])}
             (str (:authorisation/phrase authorisation))]
            [:div
             {:class (u/bem [:text :font-size-x-small :padding-left-tiny])}
             (str (:user/id authorisation))]
            [:div
             {:class (u/bem [:text :font-size-x-small :padding-left-tiny])}
             (->> (:authorisation/created-at authorisation)
                  (t.coerce/from-date)
                  (t.format/unparse (t.format/formatter "MMM dd, yyyy 'at' HH:mm.ss")))]
            [:div
             {:class (u/bem [:text :font-size-x-small :padding-left-tiny])}
             (or
              (some->> (:authorisation/granted-at authorisation)
                       (t.coerce/from-date)
                       (t.format/unparse (t.format/formatter "MMM dd, yyyy 'at' HH:mm.ss")))
              "n/a")]]))]]


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
  (let [!authorisations (re-frame/subscribe [:pages.admin.authorisations/authorisations])]
    (fn [properties views behaviours]
      [view
       (assoc properties
              :authorisations @!authorisations)
       {:route-to-home-link [link/link
                             {:label "Go home"}
                             {}
                             {:on-click #(re-frame/dispatch [:app/route :home])}]}
       {}])))
