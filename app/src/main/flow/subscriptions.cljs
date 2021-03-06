(ns flow.subscriptions
  (:require [re-frame.core :as re-frame]
            [flow.utils :as u]
            [clojure.string :as string]))


(re-frame/reg-sub
 :initialising?
 (fn [db [_]]
   (let [routing-not-initialised? (not (contains? db :route))
         current-user-id-not-received? (not (contains? db :current-user-id))
         current-user-expected? (some? (:current-user-id db))
         current-user-not-received? (not (contains? (:user db) (:current-user-id db)))]
     (or routing-not-initialised?
         current-user-id-not-received?
         (and current-user-expected?
              current-user-not-received?)))))


(re-frame/reg-sub
 :route
 (fn [db [_]]
   (:route db)))


(re-frame/reg-sub
 :authorised?
 (fn [db [_]]
   (and
    (contains? db :current-user-id)
    (some? (:current-user-id db)))))


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
   (not (u/valid-email-address? (:authorisation-email-address db)))))


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
   (get-in db [:user (:current-user-id db)])))


(re-frame/reg-sub
 :users
 (fn [db [_]]
   (vals (:user db))))


(re-frame/reg-sub
 :authorisations
 (fn [db [_]]
   (vals (:authorisation db))))


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
    (not (u/valid-email-address? (:user-addition-email-address db)))
    (string/blank? (:user-addition-name db)))))
