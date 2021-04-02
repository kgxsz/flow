(ns flow.effects
  (:require [flow.routing :as routing]
            [flow.utils :as utils]
            [ajax.core :as ajax]
            [re-frame.core :as re-frame]
            [domkm.silk :as silk]
            [pushy.core :as pushy]
            [medley.core :as medley]
            [clojure.string :as string]))


(re-frame/reg-fx
 :initialise-routing
 (fn []
   (reset! routing/!history
           (pushy/pushy
            #(re-frame/dispatch
              [:route {:route (::silk/name %)
                       :route-params (->> (medley.core/remove-keys namespace %)
                                          (medley/map-vals string/lower-case))
                       :query-params (->> % ::silk/url :query (medley/map-keys keyword))}])
            #(or (silk/arrive routing/routes %)
                 {::silk/name :unknown, ::silk/url {:query {}}})))
   (pushy/start! @routing/!history)))


(re-frame/reg-fx
 :update-route
 (fn [{:keys [route route-params query-params]}]
   (pushy/set-token! @routing/!history (silk/depart routing/routes route (or route-params {})))))


(re-frame/reg-fx
 :api
 (fn [parameters]
   ;; TODO - fix all this soon
   (ajax/POST (utils/make-url)
              {:params parameters
               :with-credentials true
               :handler (fn [response]
                          (re-frame/dispatch [:handle-api-success parameters response]))
               :error-handler (fn [{:keys [response]}]
                                (js/console.warn "API call NOT successful!")
                                (js/console.warn response)
                                (re-frame/dispatch [:handle-api-failure parameters response]))})))
