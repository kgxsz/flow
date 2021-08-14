(ns flow.effects
  (:require [flow.router :as router]
            [flow.api :as api]
            [re-frame.core :as re-frame]))


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
 ;; Move from handlers to on-response etc?
 (fn [{:keys [command query metadata session on-response on-error]}]
   (let [parameters {:command (or command {})
                     :query (or query {})
                     :metadata (or metadata {})
                     :session (or session {})}
         on-response #(re-frame/dispatch [on-response %])
         on-error #(re-frame/dispatch [on-error %])]
     (api/request! parameters on-response on-error))))
