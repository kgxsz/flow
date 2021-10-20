(ns flow.effects
  (:require [flow.router :as router]
            [flow.api :as api]
            [re-frame.core :as re-frame]))


(re-frame/reg-fx
 :router
 (fn [parameters]
   (router/update! parameters)))


(re-frame/reg-fx
 :api
 (fn [{:keys [command query metadata session on-response on-error delay]}]
   (api/request!
    {:command (or command {})
     :query (or query {})
     :metadata (or metadata {})
     :session (or session {})}
    on-response
    on-error
    delay)))
