(ns flow.router
  (:require [re-frame.core :as re-frame]
            [pushy.core :as pushy]
            [medley.core :as medley]
            [domkm.silk :as silk]
            [clojure.string :as string]))


(defonce !history (atom nil))


(def routes
  (silk/routes [[:home [[]]]
                [:admin.users [["admin" "users"]]]
                [:admin.authorisations [["admin" "authorisations"]]]]))


(defn initialise! []
  (reset! !history
          (pushy/pushy
           #(re-frame/dispatch
             [(case (::silk/name %)
                :home :pages.home/initialise
                :admin.users :pages.admin.users/initialise
                :admin.authorisations :pages.admin.authorisations/initialise
                :unknown :pages.unknown/initialise)
              {:route-params (->> (medley.core/remove-keys namespace %)
                                  (medley/map-vals string/lower-case))
               :query-params (->> % ::silk/url :query (medley/map-keys keyword))}])
           #(or (silk/arrive routes %)
                {::silk/name :unknown, ::silk/url {:query {}}}))))


(defn start! []
  (pushy/start! @!history))


(defn update! [{:keys [route route-params]}]
  (pushy/set-token!
   @!history
   (silk/depart routes route (or route-params {}))))
