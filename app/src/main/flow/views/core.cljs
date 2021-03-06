(ns flow.views.core
  (:require [re-frame.core :as re-frame]
            [flow.views.loading-page :as loading-page]
            [flow.views.home-page :as home-page]
            [flow.views.admin-page :as admin-page]
            [flow.views.unknown-page :as unknown-page]
            [flow.utils :as u]))


(defn view [{:keys [initialising?
                    route]}
            {:keys [loading-page
                    home-page
                    admin-page
                    unknown-page]}
            _]
  [:div
   {:class (u/bem [:core])}
   (if initialising?
     [loading-page]
     (case route
       :home [home-page]
       :admin [admin-page {:content :summary}]
       :admin.users [admin-page {:content :users}]
       :admin.authorisations [admin-page {:content :authorisations}]
       :unknown [unknown-page]))])


(defn core []
  (let [!initialising? (re-frame/subscribe [:initialising?])
        !route (re-frame/subscribe [:route])]
    (fn []
      [view
       {:initialising? @!initialising?
        :route @!route}
       {:loading-page loading-page/loading-page
        :home-page home-page/home-page
        :admin-page admin-page/admin-page
        :unknown-page unknown-page/unknown-page}
       {}])))
