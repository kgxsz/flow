(ns flow.views.components.router
  (:require [re-frame.core :as re-frame]
            [flow.views.pages.home-page :as home-page]
            [flow.views.pages.admin-page :as admin-page]
            [flow.views.pages.unknown-page :as unknown-page]
            [flow.utils :as u]))


(defn view [{:keys [status
                    route]}
            {:keys [home-page
                    admin-page
                    unknown-page]}
            _]
  [:div
   {:class (u/bem [:router])}
   (case status
     :initialising
     [:div
      {:class (u/bem [:cell :column :padding-top-huge])}
      [:div
       {:class (u/bem [:icon :leaf :font-size-xxx-huge])}]]

     :initialised
     (case route
       :home [home-page]
       :admin [admin-page {:content :default}]
       :admin.users [admin-page {:content :users}]
       :admin.authorisations [admin-page {:content :authorisations}]
       :unknown [unknown-page]))])


(defn router []
  (let [!status (re-frame/subscribe [:router/status])
        !route (re-frame/subscribe [:router/route])]
    (fn []
      [view
       {:status @!status
        :route @!route}
       {:home-page home-page/home-page
        :admin-page admin-page/admin-page
        :unknown-page unknown-page/unknown-page}
       {}])))
