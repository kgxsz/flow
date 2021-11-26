(ns flow.views.app
  (:require [re-frame.core :as re-frame]
            [flow.views.pages.home :as pages.home]
            [flow.views.pages.admin.users :as pages.admin.users]
            [flow.views.pages.admin.authorisations :as pages.admin.authorisations]
            [flow.views.pages.unknown :as pages.unknown]
            [flow.views.pages.loading :as pages.loading]
            [flow.utils :as u]))


(defn view [{:keys [routing?
                    error?]}
            {:keys [page]}
            _]
  [:div
   {:class (u/bem [:app])}
   (when routing?
     [:div
      {:class (u/bem [:app__loader])}])
   (if error?
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
         "Something has gone wrong"]]]]
     page)])


(defn app [properties views behaviours]
  (let [!routing? (re-frame/subscribe [:app/routing?])
        !error? (re-frame/subscribe [:app/error?])
        !admin? (re-frame/subscribe [:app/admin?])
        !authorised? (re-frame/subscribe [:app/authorised?])
        !current-user (re-frame/subscribe [:app/current-user])
        !route (re-frame/subscribe [:app/route key])]
    (fn []
      [view
       {:routing? @!routing?
        :error? @!error?}
       {:page (case @!route
                :home [pages.home/page
                       {:authorised? @!authorised?
                        :current-user @!current-user}]
                :admin.users [pages.admin.users/page
                              {:admin? @!admin?
                               :authorised? @!authorised?
                               :current-user @!current-user}]
                :admin.authorisations [pages.admin.authorisations/page
                                       {:admin? @!admin?
                                        :authorised? @!authorised?
                                        :current-user @!current-user}]
                :unknown [pages.unknown/page
                          {:authorised? @!authorised?
                           :current-user @!current-user}]
                [pages.loading/page
                 {}
                 {}
                 {}])}
       {}])))
