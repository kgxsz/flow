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
 :query
 (fn [query]
   (ajax/POST (utils/make-url :query)
              {:params query
               :with-credentials true
               :handler (fn [response]
                          (re-frame/dispatch [:query-success query response]))
               :error-handler (fn [{:keys [response]}] (re-frame/dispatch [:query-failure query response]))})))


(re-frame/reg-fx
 :command
 (fn [command]
   (ajax/POST (utils/make-url :command)
              {:params command
               :with-credentials true
               :handler (fn [response] (re-frame/dispatch [:command-success command response]))
               :error-handler (fn [{:keys [response]}] (re-frame/dispatch [:command-failure command response]))})))
