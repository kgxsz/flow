(ns flow.views.core
  (:require [re-frame.core :as re-frame]
            [flow.views.pages.initialisation-page :as initialisation-page]
            [flow.views.pages.home-page :as home-page]
            [flow.views.pages.admin-page :as admin-page]
            [flow.views.pages.unknown-page :as unknown-page]
            [flow.utils :as u]))


(defn view [{:keys [initialising?
                    route]}
            {:keys [initialisation-page
                    home-page
                    admin-page
                    unknown-page]}
            _]
  [:div
   {:class (u/bem [:core])}
   (if initialising?
     [initialisation-page]
     (case route
       :home [home-page]
       :admin [admin-page {:content :default}]
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
       {:initialisation-page initialisation-page/initialisation-page
        :home-page home-page/home-page
        :admin-page admin-page/admin-page
        :unknown-page unknown-page/unknown-page}
       {}])))
