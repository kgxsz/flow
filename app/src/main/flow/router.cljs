(ns flow.router
  (:require [re-frame.core :as re-frame]
            [pushy.core :as pushy]
            [medley.core :as medley]
            [domkm.silk :as silk]
            [clojure.string :as string]))


(defonce !history (atom nil))


(def routes (silk/routes [[:home [[]]]
                          [:admin [["admin"]]]
                          [:admin.users [["admin" "users"]]]
                          [:admin.authorisations [["admin" "authorisations"]]]]))


(defn initialised?
  []
  (some? @!history))


(defn initialise!
  [on-update on-initialised]
  (reset! !history
          (pushy/pushy
           #(on-update {:route (::silk/name %)
                        :route-params (->> (medley.core/remove-keys namespace %)
                                           (medley/map-vals string/lower-case))
                        :query-params (->> % ::silk/url :query (medley/map-keys keyword))})
           #(or (silk/arrive routes %)
                {::silk/name :unknown, ::silk/url {:query {}}})))
  (pushy/start! @!history)
  (on-initialised))


(defn update! [{:keys [route route-params]}]
  (pushy/set-token!
   @!history
   (silk/depart routes route (or route-params {}))))
