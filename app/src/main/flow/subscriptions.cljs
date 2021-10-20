(ns flow.subscriptions
  (:require [re-frame.core :as re-frame]
            [flow.utils :as u]
            [cljs.spec.alpha :as s]
            [clojure.string :as string]))


;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;
;;;;;;;;;;;;;;;;;;;;;;;; App flow ;;;;;;;;;;;;;;;;;;;;;;;;
;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;

(re-frame/reg-sub
 :app/route
 (fn [db [_]]
   (let [key [:views :app]
         context (get-in db key)]
     (get-in context [:routing :route]))))


(re-frame/reg-sub
 :app/routing?
 (fn [db [_]]
   (let [key [:views :app]
         context (get-in db key)]
     (contains? #{:routing} (:status context)))))


(re-frame/reg-sub
 :app/error?
 (fn [db [_]]
   (let [key [:views :app]
         context (get-in db key)]
     (contains? #{:error} (:status context)))))


(re-frame/reg-sub
 :app/authorised?
 (fn [db [_]]
   (let [key [:views :app]
         context (get-in db key)]
     (some? (get-in context [:session :current-user-id])))))


(re-frame/reg-sub
 :app/current-user
 (fn [db [_]]
   (let [key [:views :app]
         context (get-in db key)]
     (get-in db [:entities :users (get-in context [:session :current-user-id])]))))



;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;
;;;;;;;;;;;;;;;;;;;;;; Input flow ;;;;;;;;;;;;;;;;;;;;;;;;
;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;

(re-frame/reg-sub
 :input/value
 (fn [db [_ key]]
   (:value (get-in db key))))


;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;
;;;;;;;;;;;;;;;;;;;;; Button flow ;;;;;;;;;;;;;;;;;;;;;;;;
;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;

;; NOTE - Experimental
(re-frame/reg-sub
 :button/pending?
 (fn [db [_ key]]
   (let [context (get-in db key)]
     (contains? #{:pending} (:status context)))))



;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;
;;;;;;;;;;;;;;; Authorisation attempt flow ;;;;;;;;;;;;;;;
;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;

(re-frame/reg-sub
 :authorisation-attempt/status
 (fn [db [_]]
   (let [key [:views :app :views :pages.home :views :authorisation-attempt]
         context (get-in db key)]
     (:status context))))


(re-frame/reg-sub
 :authorisation-attempt/email-address-update-disabled?
 (fn [db [_]]
   (let [key [:views :app :views :pages.home :views :authorisation-attempt]
         context (get-in db key)]
     (not (contains? #{:idle} (:status context))))))


(re-frame/reg-sub
 :authorisation-attempt/initialisation-disabled?
 (fn [db [_]]
   (let [key [:views :app :views :pages.home :views :authorisation-attempt]
         context (get-in db key)
         email-address (get-in context [:views :email-address-input :value])]
     (not
      (and
       (contains? #{:idle :initialising} (:status context))
       (s/valid? :user/email-address email-address))))))


(re-frame/reg-sub
 :authorisation-attempt/phrase-update-disabled?
 (fn [db [_]]
   (let [key [:views :app :views :pages.home :views :authorisation-attempt]
         context (get-in db key)]
     (not (contains? #{:initialised :finalised-unsuccessfully} (:status context))))))


(re-frame/reg-sub
 :authorisation-attempt/finalisation-disabled?
 (fn [db [_]]
   (let [key [:views :app :views :pages.home :views :authorisation-attempt]
         context (get-in db key)
         phrase (get-in context [:views :phrase-input :value])]
     (not
      (and
       (contains? #{:initialised :finalising :finalised-unsuccessfully} (:status context))
       (s/valid? :authorisation/phrase phrase))))))



;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;
;;;;;;;;;;;;;;; Admin user page flow ;;;;;;;;;;;;;;;;;;;;;
;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;

(re-frame/reg-sub
 :pages.admin.users/users
 (fn [db [_]]
   (vals (get-in db [:entities :users]))))



;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;
;;;;;;;;; Admin authorisations page flow ;;;;;;;;;;;;;;;;;
;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;

(re-frame/reg-sub
 :pages.admin.authorisations/authorisations
 (fn [db [_]]
   (vals (get-in db [:entities :authorisations]))))

;;;;;;;;;;;;;;;;;;;;;


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
