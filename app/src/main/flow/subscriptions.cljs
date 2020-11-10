(ns flow.subscriptions
  (:require [re-frame.core :as re-frame]))


(re-frame/reg-sub
 :initialising?
 (fn [db [_]]
   (let [routing-not-initialised? (not (contains? db :route))]
     routing-not-initialised?)))


(re-frame/reg-sub
 :route
 (fn [db [_]]
   (:route db)))
