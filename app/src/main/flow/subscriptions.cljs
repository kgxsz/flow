(ns flow.subscriptions
  (:require [re-frame.core :as re-frame]
            [flow.utils :as u]
            [clojure.string :as string]))


(re-frame/reg-sub
 :initialising?
 (fn [db [_]]
   (let [routing-established? (contains? db :routing)
         session-established? (contains? db :session)]
     (or (not routing-established?)
         (not session-established?)))))


(re-frame/reg-sub
 :route
 (fn [db [_]]
   (get-in db [:routing :route])))


(re-frame/reg-sub
 :authorised?
 (fn [db [_]]
   (some? (get-in db [:session :current-user-id]))))


(re-frame/reg-sub
 :authorisation-email-address
 (fn [db [_]]
   (:authorisation-email-address db)))


(re-frame/reg-sub
 :authorisation-phrase
 (fn [db [_]]
   (:authorisation-phrase db)))


(re-frame/reg-sub
 :authorisation-initialised?
 (fn [db [_]]
   (:authorisation-initialised? db)))


(re-frame/reg-sub
 :authorisation-finalised?
 (fn [db [_]]
   (:authorisation-finalised? db)))


(re-frame/reg-sub
 :authorisation-initialisation-disabled?
 (fn [db [_]]
   (not (u/email-address? (:authorisation-email-address db)))))


(re-frame/reg-sub
 :authorisation-finalisation-disabled?
 (fn [db [_]]
   (let [{:keys [authorisation-phrase]} db]
     (or (string/blank? authorisation-phrase)
         (< (count authorisation-phrase) 3)))))


(re-frame/reg-sub
 :authorisation-failed?
 (fn [db [_]]
   (true? (:authorisation-failed? db))))


(re-frame/reg-sub
 :current-user
 (fn [db [_]]
   (let [{:keys [current-user-id]} (:session db)]
     (get-in db [:users current-user-id]))))


(re-frame/reg-sub
 :users
 (fn [db [_]]
   (vals (:users db))))


(re-frame/reg-sub
 :authorisations
 (fn [db [_]]
   (vals (:authorisations db))))


(re-frame/reg-sub
 :user-addition-name
 (fn [db [_]]
   (:user-addition-name db)))


(re-frame/reg-sub
 :user-addition-email-address
 (fn [db [_]]
   (:user-addition-email-address db)))


(re-frame/reg-sub
 :user-addition-admin-role?
 (fn [db [_]]
   ;; TODO - if the key doesn't exist, what should be done here?
   (:user-addition-admin-role? db)))


(re-frame/reg-sub
 :user-addition-disabled?
 (fn [db [_]]
   (or
    (not (u/email-address? (:user-addition-email-address db)))
    (string/blank? (:user-addition-name db)))))
