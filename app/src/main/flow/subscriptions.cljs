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
 :authorisation-attempt/email-address
 (fn [db [_]]
   (let [key [:views :app :views :pages.home :views :authorisation-attempt]
         context (get-in db key)]
     (:email-address context))))


(re-frame/reg-sub
 :authorisation-attempt/initialisation-disabled?
 (fn [db [_]]
   (let [key [:views :app :views :pages.home :views :authorisation-attempt]
         context (get-in db key)]
     (not
      (and
       (contains? #{:idle :initialising} (:status context))
       (s/valid? :user/email-address (:email-address context)))))))


(re-frame/reg-sub
 :authorisation-attempt/initialising?
 (fn [db [_]]
   (let [key [:views :app :views :pages.home :views :authorisation-attempt]
         context (get-in db key)]
     (contains? #{:initialising} (:status context)))))


(re-frame/reg-sub
 :authorisation-attempt/phrase-update-disabled?
 (fn [db [_]]
   (let [key [:views :app :views :pages.home :views :authorisation-attempt]
         context (get-in db key)]
     (not (contains? #{:initialised :finalised-unsuccessfully} (:status context))))))


(re-frame/reg-sub
 :authorisation-attempt/phrase
 (fn [db [_]]
   (let [key [:views :app :views :pages.home :views :authorisation-attempt]
         context (get-in db key)]
     (:phrase context))))


(re-frame/reg-sub
 :authorisation-attempt/finalisation-disabled?
 (fn [db [_]]
   (let [key [:views :app :views :pages.home :views :authorisation-attempt]
         context (get-in db key)]
     (not
      (and
       (contains? #{:initialised :finalising :finalised-unsuccessfully} (:status context))
       (s/valid? :authorisation/phrase (:phrase context)))))))


(re-frame/reg-sub
 :authorisation-attempt/finalising?
 (fn [db [_]]
   (let [key [:views :app :views :pages.home :views :authorisation-attempt]
         context (get-in db key)]
     (contains? #{:finalising} (:status context)))))



;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;
;;;;;;;;;;;;;;; Admin user page flow ;;;;;;;;;;;;;;;;;;;;;
;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;

(re-frame/reg-sub
 :pages.admin.users/ids
 (fn [db [_]]
   (keys (get-in db [:entities :users]))))



;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;
;;;;;;;;; Admin authorisations page flow ;;;;;;;;;;;;;;;;;
;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;

(re-frame/reg-sub
 :pages.admin.authorisations/ids
 (fn [db [_]]
   (keys (get-in db [:entities :authorisations]))))



;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;
;;;;;;;;;;;;;;;;;;;; User flow ;;;;;;;;;;;;;;;;;;;;;;;;;;;
;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;


(re-frame/reg-sub
 :user/user
 (fn [db [_ id]]
   (get-in db [:entities :users id])))


(re-frame/reg-sub
 :user/deletion-disabled?
 (fn [db [_ id]]
   (let [current-user-id (get-in db [:views :app :session :current-user-id])
         user (get-in db [:entities :users id])]
     (or
      (= id (get-in db [:views :app :session :current-user-id]))
      (some? (get-in db [:entities :users id :user/deleted-at]))))))


(re-frame/reg-sub
 :user/deleting?
 (fn [db [_ id]]
   (let [key [:views :app :views :pages.admin.users :views :user id]
         context (get-in db key)]
     (contains? #{:deleting} (:status context)))))


;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;
;;;;;;;;;;;;;;;; Authoirsation flow ;;;;;;;;;;;;;;;;;;;;;;
;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;


(re-frame/reg-sub
 :authorisation/authorisation
 (fn [db [_ id]]
   (get-in db [:entities :authorisations id])))


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
