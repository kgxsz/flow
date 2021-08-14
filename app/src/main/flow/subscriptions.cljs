(ns flow.subscriptions
  (:require [re-frame.core :as re-frame]
            [flow.utils :as u]
            [cljs.spec.alpha :as s]
            [clojure.string :as string]))


;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;
;;;;;;;;;;;;;;;;;;;;;; Router flow ;;;;;;;;;;;;;;;;;;;;;;;
;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;

(re-frame/reg-sub
 :router/status
 (fn [db [_]]
   (get-in db [:flows :router :status])))


(re-frame/reg-sub
 :router/route
 (fn [db [_]]
   (get-in db [:flows :router :route])))


;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;
;;;;;;;;;;;;;;;;;;;; Home page flow ;;;;;;;;;;;;;;;;;;;;;;
;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;

(re-frame/reg-sub
 :home-page/status
 (fn [db [_]]
   (get-in db [:flows :home-page :status])))


(re-frame/reg-sub
 :home-page/authorised?
 (fn [db [_]]
   (some? (get-in db [:flows :home-page :current-user-id]))))


;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;
;;;;;;;;;;;;;;;;;; Unknown page flow ;;;;;;;;;;;;;;;;;;;;;
;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;

(re-frame/reg-sub
 :unknown-page/status
 (fn [db [_]]
   (get-in db [:flows :unknown-page :status])))


;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;
;;;;;;;;;;;;;;; Authorisation attempt flow ;;;;;;;;;;;;;;;
;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;

(re-frame/reg-sub
 :authorisation-attempt/status
 (fn [db [_]]
   (get-in db [:flows :authorisation-attempt :status])))


(re-frame/reg-sub
 :authorisation-attempt/initialising?
 (fn [db [_]]
   (let [status (get-in db [:flows :authorisation-attempt :status])]
     (= status :initialising))))


(re-frame/reg-sub
 :authorisation-attempt/finalising?
 (fn [db [_]]
   (let [status (get-in db [:flows :authorisation-attempt :status])]
     (= status :finalising))))


(re-frame/reg-sub
 :authorisation-attempt/email-address
 (fn [db [_]]
   (get-in db [:flows :authorisation-attempt :user/email-address])))


(re-frame/reg-sub
 :authorisation-attempt/email-address-update-disabled?
 (fn [db [_]]
   (let [{:keys [status]} (get-in db [:flows :authorisation-attempt])]
     (not (contains? #{:uninitialised} status)))))


(re-frame/reg-sub
 :authorisation-attempt/phrase
 (fn [db [_]]
   (get-in db [:flows :authorisation-attempt :authorisation/phrase])))


 (re-frame/reg-sub
  :authorisation-attempt/phrase-update-disabled?
  (fn [db [_]]
    (let [{:keys [status]} (get-in db [:flows :authorisation-attempt])]
      (not (contains? #{:initialised :unsuccessfully-finalised} status)))))


(re-frame/reg-sub
 :authorisation-attempt/initialisation-disabled?
 (fn [db [_]]
   (let [{:keys [status]} (get-in db [:flows :authorisation-attempt])
         email-address (get-in db [:flows :authorisation-attempt :user/email-address])]
     (not
      (and
       (contains? #{:uninitialised :initialising} status)
       (s/valid? :user/email-address email-address))))))


(re-frame/reg-sub
 :authorisation-attempt/finalisation-disabled?
 (fn [db [_]]
   (let [{:keys [status]} (get-in db [:flows :authorisation-attempt])
         phrase (get-in db [:flows :authorisation-attempt :authorisation/phrase])]
     (not
      (and
       (contains? #{:initialised :finalising :unsuccessfully-finalised} status)
       (s/valid? :authorisation/phrase phrase))))))


#_(re-frame/reg-sub
 :current-user
 (fn [db [_]]
   (let [{:keys [current-user-id]} (:session db)]
     (get-in db [:users current-user-id]))))


#_(re-frame/reg-sub
 :users
 (fn [db [_]]
   (vals (:users db))))


#_(re-frame/reg-sub
 :authorisations
 (fn [db [_]]
   (vals (:authorisations db))))


#_(re-frame/reg-sub
 :user-addition-name
 (fn [db [_]]
   (:user-addition-name db)))


#_(re-frame/reg-sub
 :user-addition-email-address
 (fn [db [_]]
   (:user-addition-email-address db)))


#_(re-frame/reg-sub
 :user-addition-admin-role?
 (fn [db [_]]
   ;; TODO - if the key doesn't exist, what should be done here?
   (:user-addition-admin-role? db)))


#_(re-frame/reg-sub
 :user-addition-disabled?
 (fn [db [_]]
   (or
    (not (u/email-address? (:user-addition-email-address db)))
    (string/blank? (:user-addition-name db)))))
