(ns flow.subscriptions
  (:require [re-frame.core :as re-frame]
            [flow.utils :as u]
            [cljs.spec.alpha :as s]
            [clojure.string :as string]))


;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;
;;;;;;;;;;;;;;;;;;;;;;;; App ;;;;;;;;;;;;;;;;;;;;;;;;;;;;;
;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;

(re-frame/reg-sub
 :app/route
 (fn [db [_]]
   (get-in db [:router :route])))


(re-frame/reg-sub
 :app/routing?
 (fn [db [_]]
   (true? (get-in db [:router :routing?]))))


(re-frame/reg-sub
 :app/error?
 (fn [db [_]]
   (true? (get-in db [:notifier :error?]))))


(re-frame/reg-sub
 :app/authorised?
 (fn [db [_]]
   (some? (get-in db [:session :current-user-id]))))


(re-frame/reg-sub
 :app/admin?
 (fn [db [_]]
   (let [current-user-id (get-in db [:session :current-user-id])
         roles (get-in db [:entities :users current-user-id :user/roles])]
     (contains? roles :admin))))


(re-frame/reg-sub
 :app/current-user
 (fn [db [_]]
   (let [current-user-id (get-in db [:session :current-user-id])]
     (get-in db [:entities :users current-user-id]))))



;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;
;;;;;;;;;;;;;;;;;; Cards user ;;;;;;;;;;;;;;;;;;;;;;;;;;;;
;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;

(re-frame/reg-sub
 :cards.user/user
 (fn [db [_ id]]
   (get-in db [:entities :users id])))



;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;
;;;;;;;;;;;;;; Cards authorisation ;;;;;;;;;;;;;;;;;;;;;;;
;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;

(re-frame/reg-sub
 :cards.authorisation/authorisation
 (fn [db [_ id]]
   (get-in db [:entities :authorisations id])))



;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;
;;;;;;;;;;;;;;;;;;; Pagination ;;;;;;;;;;;;;;;;;;;;;;;;;;;
;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;

(re-frame/reg-sub
 :pagination/ids
 (fn [db [_ key entity-type]]
   (let [entity-key (case entity-type
                      :users :user/id
                      :authorisations :authorisation/id)]
     (->> (get-in db [:entities entity-type])
          (vals)
          (sort-by (comp str entity-key))
          (map entity-key)))))


(re-frame/reg-sub
 :pagination/exhausted?
 (fn [db [_ key]]
   (let [context (get-in db key)]
     (:exhausted? context))))


(re-frame/reg-sub
 :pagination/pending?
 (fn [db [_ key]]
   (let [context (get-in db key)]
     (contains? #{:pending} (:status context)))))



;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;
;;;;;;;;;;;;;;;;;;; Authorisation ;;;;;;;;;;;;;;;;;;;;;;;;
;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;

(re-frame/reg-sub
 :authorisation/status
 (fn [db [_ key]]
   (let [context (get-in db key)]
     (:status context))))


(re-frame/reg-sub
 :authorisation/email-address-update-disabled?
 (fn [db [_ key]]
   (let [context (get-in db key)]
     (not (contains? #{:idle} (:status context))))))


(re-frame/reg-sub
 :authorisation/email-address
 (fn [db [_ key]]
   (let [context (get-in db key)]
     (:email-address context))))


(re-frame/reg-sub
 :authorisation/initialisation-disabled?
 (fn [db [_ key]]
   (let [context (get-in db key)]
     (not
      (and
       (contains? #{:idle :initialisation-pending} (:status context))
       (s/valid? :user/email-address (:email-address context)))))))


(re-frame/reg-sub
 :authorisation/initialisation-pending?
 (fn [db [_ key]]
   (let [context (get-in db key)]
     (contains? #{:initialisation-pending} (:status context)))))


(re-frame/reg-sub
 :authorisation/phrase-update-disabled?
 (fn [db [_ key]]
   (let [context (get-in db key)]
     (not
      (contains?
       #{:initialisation-successful
         :finalisation-unsuccessful}
       (:status context))))))


(re-frame/reg-sub
 :authorisation/phrase
 (fn [db [_ key]]
   (let [context (get-in db key)]
     (:phrase context))))


(re-frame/reg-sub
 :authorisation/finalisation-disabled?
 (fn [db [_ key]]
   (let [context (get-in db key)]
     (not
      (and
       (contains?
        #{:initialisation-successful
          :finalisation-pending
          :finalisation-unsuccessful}
        (:status context))
       (s/valid? :authorisation/phrase (:phrase context)))))))


(re-frame/reg-sub
 :authorisation/finalisation-pending?
 (fn [db [_ key]]
   (let [context (get-in db key)]
     (contains? #{:finalisation-pending} (:status context)))))



;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;
;;;;;;;;;;;;;;;;; Deauthorisation ;;;;;;;;;;;;;;;;;;;;;;;;
;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;

(re-frame/reg-sub
 :deauthorisation/pending?
 (fn [db [_ key]]
   (let [context (get-in db key)]
     (contains? #{:pending} (:status context)))))



;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;
;;;;;;;;;;;;;;;;;; User addition ;;;;;;;;;;;;;;;;;;;;;;;;;
;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;

(re-frame/reg-sub
 :user-addition/status
 (fn [db [_ key]]
   (let [context (get-in db key)]
     (:status context))))


(re-frame/reg-sub
 :user-addition/email-address
 (fn [db [_ key]]
   (let [context (get-in db key)]
     (:email-address context))))


(re-frame/reg-sub
 :user-addition/name
 (fn [db [_ key]]
   (let [context (get-in db key)]
     (:name context))))


(re-frame/reg-sub
 :user-addition/admin-role?
 (fn [db [_ key]]
   (let [context (get-in db key)]
     (contains? (:roles context) :admin))))


(re-frame/reg-sub
 :user-addition/disabled?
 (fn [db [_ key]]
   (let [context (get-in db key)]
     (or
      (not (s/valid? :user/name (:name context)))
      (not (s/valid? :user/email-address (:email-address context)))
      (not (s/valid? :user/roles (:roles context)))))))


(re-frame/reg-sub
 :user-addition/pending?
 (fn [db [_ key]]
   (let [context (get-in db key)]
     (contains? #{:pending} (:status context)))))



;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;
;;;;;;;;;;;;;;;;; User deletion ;;;;;;;;;;;;;;;;;;;;;;;;;;
;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;

(re-frame/reg-sub
 :user-deletion/disabled?
 (fn [db [_ id]]
   (let [current-user-id (get-in db [:views :app :session :current-user-id])
         user (get-in db [:entities :users id])]
     (or
      (= id (get-in db [:views :app :session :current-user-id]))
      (some? (get-in db [:entities :users id :user/deleted-at]))))))


(re-frame/reg-sub
 :user-deletion/pending?
 (fn [db [_ key]]
   (let [context (get-in db key)]
     (contains? #{:pending} (:status context)))))
