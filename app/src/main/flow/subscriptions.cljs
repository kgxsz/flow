(ns flow.subscriptions
  (:require [re-frame.core :as re-frame]))


(re-frame/reg-sub
 :initialising?
 (fn [db [_]]
   (let [routing-not-initialised? (not (contains? db :route))
         current-user-id-not-received? (not (contains? db :current-user-id))]
     (or routing-not-initialised?
         current-user-id-not-received?))))


(re-frame/reg-sub
 :authorised?
 (fn [db [_]]
   (when (contains? db :current-user-id)
     (and (some? (:current-user-id db))
          (contains? (:user db) (:current-user-id db))))))


(re-frame/reg-sub
 :route
 (fn [db [_]]
   (:route db)))


(re-frame/reg-sub
 :input-value
 (fn [db [_]]
   (:input-value db)))
