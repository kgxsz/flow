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
 :router
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
 (fn [{:keys [command query metadata session handlers]}]
   (ajax/POST (utils/make-url)
              {:params {:command (or command {})
                        :query (or query {})
                        :metadata (or metadata {})
                        :session (or session {})}
               :with-credentials true
               :handler (fn [response]
                          (re-frame/dispatch [(:success handlers) response]))
               :error-handler (fn [{:keys [response]}]
                                (re-frame/dispatch [(:error handlers) response]))
               :response-format (ajax/transit-response-format {:handlers {"u" ->UUID "n" long}}) })))
