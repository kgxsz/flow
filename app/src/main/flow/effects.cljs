(ns flow.effects
  (:require [flow.router :as router]
            [flow.api :as api]
            [re-frame.core :as re-frame]
            [cljs.core.async :refer [<!]]
            [cljs.core.async :refer-macros [go]]))


(re-frame/reg-fx
 :router
 (fn [parameters]
   (if (router/initialised?)
     (router/update! parameters)
     (let [on-update #(re-frame/dispatch [:router/updated %])
           on-initialised #(re-frame/dispatch [:router/initialisation-ended])]
       (router/initialise! on-update on-initialised)))))


(re-frame/reg-fx
 :api
 (fn [{:keys [command query metadata session on-response on-error delay]}]
   (let [parameters {:command (or command {})
                     :query (or query {})
                     :metadata (or metadata {})
                     :session (or session {})}
         on-response #(go (<! delay) (re-frame/dispatch [on-response %]))
         on-error #(go (<! delay) (re-frame/dispatch [on-error %]))]
     (api/request! parameters on-response on-error))))
