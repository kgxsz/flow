(ns flow.events
  (:require [re-frame.core :as re-frame]
            [flow.interceptors :as interceptors]
            [flow.utils :as u]
            [clojure.string :as string]
            [cljs.core.async :refer [timeout]]))


;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;
;;;;;;;;;;;;;;;;;;;;;;; App flow ;;;;;;;;;;;;;;;;;;;;;;;;;
;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;

(re-frame/reg-event-fx
 :app/initialise
 [interceptors/validate-db]
 (fn [{:keys [db]} [_]]
   (let [key [:views :app]]
     {:db (update-in db key assoc :status :routing)})))


(re-frame/reg-event-fx
 :app/route
 [interceptors/validate-db]
 (fn [{:keys [db]} [_ route]]
   (let [key [:views :app]]
     {:router {:route route}
      :db (update-in db key assoc :status :routing)})))


(re-frame/reg-event-fx
 :app/error
 [interceptors/validate-db]
 (fn [{:keys [db]} [_]]
   (let [key [:views :app]]
     {:db (update-in db key assoc :status :error)})))



;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;
;;;;;;;;;;;;;;;;;;;; Home page flow ;;;;;;;;;;;;;;;;;;;;;;
;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;

(re-frame/reg-event-fx
 :pages.home/initialise
 [interceptors/validate-db]
 (fn [{:keys [db]} [_ {:keys [route-params query-params]}]]
   (let [key [:views :app]]
     {:api {:query {:current-user {}}
            :on-response :pages.home/complete-initialisation
            :on-error :app/error
            :delay (timeout 1000)}
      :db (update-in db key assoc :status :routing)})))


(re-frame/reg-event-fx
 :pages.home/complete-initialisation
 [interceptors/validate-db]
 (fn [{:keys [db]} [_ {:keys [users session]}]]
   {:db (-> db
            (assoc-in [:views :app :status] :idle)
            (assoc-in [:views :app :routing :route] :home)
            (assoc-in [:views :app :routing :route-params] nil)
            (assoc-in [:views :app :routing :query-params] nil)
            (assoc-in [:views :app :session] session)
            (assoc-in [:views :app :views :pages.home :views :authorisation-attempt :status] :idle)
            (assoc-in [:views :app :views :pages.home :views :authorisation-attempt :email-address] "")
            (assoc-in [:views :app :views :pages.home :views :authorisation-attempt :phrase] "")
            (update-in [:entities :users] merge users))}))


(re-frame/reg-event-fx
 :pages.home/deauthorise
 [interceptors/validate-db]
 (fn [{:keys [db]} [_]]
   {:api {:command {:deauthorise {}}
          :on-response :pages.home/complete-deauthorisation
          :on-error :app/error
          :delay (timeout 1000)}
    :db (-> db
            (update-in [:views :app] dissoc :session)
            (assoc-in [:views :app :views :pages.home :views :authorisation-attempt] {:status :idle})
            (assoc-in [:views :app :views :pages.home :views :authorisation-attempt :email-address] "")
            (assoc-in [:views :app :views :pages.home :views :authorisation-attempt :phrase] "")
            (assoc-in [:entities] {}))}))


(re-frame/reg-event-fx
 :pages.home/complete-deauthorisation
 [interceptors/validate-db]
 (fn [{:keys [db]} [_ {:keys [session]}]]
   {:db (assoc-in db [:views :app :session] session)}))



;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;
;;;;;;;;;;;;;;; Admin users page flow ;;;;;;;;;;;;;;;;;;;;
;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;

(re-frame/reg-event-fx
 :pages.admin.users/initialise
 [interceptors/validate-db]
 (fn [{:keys [db]} [_ {:keys [route-params query-params]}]]
   {:api {:query {:current-user {}
                  :users {}}
          :on-response :pages.admin.users/complete-initialisation
          :on-error :app/error
          :delay (timeout 1000)}
    :db (assoc-in db [:views :app :status] :routing)}))


(re-frame/reg-event-fx
 :pages.admin.users/complete-initialisation
 [interceptors/validate-db]
 (fn [{:keys [db]} [_ {:keys [users session]}]]
   {:db (-> db
            (assoc-in [:views :app :status] :idle)
            (assoc-in [:views :app :routing :route] :admin.users)
            (assoc-in [:views :app :routing :route-params] nil)
            (assoc-in [:views :app :routing :query-params] nil)
            (assoc-in [:views :app :session] session)
            (update-in [:entities :users] merge users))}))



;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;
;;;;;;;;; Admin authorisations page flow ;;;;;;;;;;;;;;;;;
;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;

(re-frame/reg-event-fx
 :pages.admin.authorisations/initialise
 [interceptors/validate-db]
 (fn [{:keys [db]} [_ {:keys [route-params query-params]}]]
   {:api {:query {:current-user {}
                  :authorisations {}}
          :on-response :pages.admin.authorisations/complete-initialisation
          :on-error :app/error
          :delay (timeout 1000)}
    :db (assoc-in db [:views :app :status] :routing)}))


(re-frame/reg-event-fx
 :pages.admin.authorisations/complete-initialisation
 [interceptors/validate-db]
 (fn [{:keys [db]} [_ {:keys [users authorisations session]}]]
   {:db (-> db
            (assoc-in [:views :app :status] :idle)
            (assoc-in [:views :app :routing :route] :admin.authorisations)
            (assoc-in [:views :app :routing :route-params] nil)
            (assoc-in [:views :app :routing :query-params] nil)
            (assoc-in [:views :app :session] session)
            (update-in [:entities :users] merge users)
            (update-in [:entities :authorisations] merge authorisations))}))



;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;
;;;;;;;;;;;;;;;;;; Unknown page flow ;;;;;;;;;;;;;;;;;;;;;
;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;


(re-frame/reg-event-fx
 :pages.unknown/initialise
 [interceptors/validate-db]
 (fn [{:keys [db]} [_ {:keys [route-params query-params]}]]
   {:api {:query {:current-user {}}
          :on-response :pages.unknown/complete-initialisation
          :on-error :app/error
          :delay (timeout 1000)}
    :db (assoc-in db [:views :app :status] :routing)}))


(re-frame/reg-event-fx
 :pages.unknown/complete-initialisation
 [interceptors/validate-db]
 (fn [{:keys [db]} [_ {:keys [users session]}]]
   {:db (-> db
            (assoc-in [:views :app :status] :idle)
            (assoc-in [:views :app :routing :route] :unknown)
            (assoc-in [:views :app :routing :route-params] nil)
            (assoc-in [:views :app :routing :query-params] nil)
            (assoc-in [:views :app :session] session)
            (update-in [:entities :users] merge users))}))



;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;
;;;;;;;;;;;;;;; Authorisation attempt flow ;;;;;;;;;;;;;;;
;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;


(re-frame/reg-event-fx
 :authorisation-attempt/update-email-address
 [interceptors/validate-db]
 (fn [{:keys [db]} [_ value]]
   (let [key [:views :app :views :pages.home :views :authorisation-attempt]
         valid-length? (<= (count value) 250)
         sanitise #(string/replace % #"\n|\r| " "")]
     (if valid-length?
       {:db (update-in db key assoc :email-address (sanitise value))}
       {:db db}))))


(re-frame/reg-event-fx
 :authorisation-attempt/initialise
 [interceptors/validate-db]
 (fn [{:keys [db]} [_]]
   (let [key [:views :app :views :pages.home :views :authorisation-attempt]
         context (get-in db key)]
     {:api {:command {:initialise-authorisation-attempt
                      {:user/email-address (:email-address context)}}
            :on-response :authorisation-attempt/complete-initialisation
            :on-error :app/error
            :delay (timeout 1000)}
      :db (update-in db key assoc :status :initialising)})))


(re-frame/reg-event-fx
 :authorisation-attempt/complete-initialisation
 [interceptors/validate-db]
 (fn [{:keys [db]} [_ response]]
   (let [key [:views :app :views :pages.home :views :authorisation-attempt]]
     {:db (update-in db key assoc :status :initialised)})))


(re-frame/reg-event-fx
 :authorisation-attempt/update-phrase
 [interceptors/validate-db]
 (fn [{:keys [db]} [_ value]]
   (let [key [:views :app :views :pages.home :views :authorisation-attempt]
         valid-length? (<= (count value) 250)
         sanitise #(string/replace % #"\n|\r| " "")]
     (if valid-length?
       {:db (update-in db key assoc :phrase (sanitise value))}
       {:db db}))))


(re-frame/reg-event-fx
 :authorisation-attempt/finalise
 [interceptors/validate-db]
 (fn [{:keys [db]} [_]]
   (let [key [:views :app :views :pages.home :views :authorisation-attempt]
         context (get-in db [:views :app :views :pages.home :views :authorisation-attempt])]
     {:api {:command {:finalise-authorisation-attempt
                      {:user/email-address (:email-address context)
                       :authorisation/phrase (:phrase context)}}
            :query {:current-user {}}
            :on-response :authorisation-attempt/complete-finalisation
            :on-error :app/error
            :delay (timeout 1000)}
      :db (update-in db key assoc :status :finalising)})))


(re-frame/reg-event-fx
 :authorisation-attempt/complete-finalisation
 [interceptors/validate-db]
 (fn [{:keys [db]} [_ {:keys [users session]}]]
   (let [key [:views :app :views :pages.home :views :authorisation-attempt]]
     (if (:current-user-id session)
       {:db (-> db
                (update-in key assoc :status :finalised-successfully)
                (assoc-in [:views :app :session] session)
                (update-in [:entities :users] merge users))}
       {:db (update-in db key assoc :status :finalised-unsuccessfully)}))))


;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;



#_(re-frame/reg-event-fx
 :delete-user
 [interceptors/validate-db]
 (fn [{:keys [db]} [_ user-id]]
   {:api {:command {:delete-user {:user/id user-id}}
          :query {:user {:user/id user-id}}
          :on-response :core/todo
          :on-error :core/todo
          :delay (timeout 1000)}
    :db db}))


#_(re-frame/reg-event-fx
 :update-user-addition-name
 [interceptors/validate-db]
 (fn [{:keys [db]} [_ input-value]]
   (let [valid-length? (<= (count input-value) 250)
         sanitise #(string/replace % #"\n|\r| " "")]
     (if valid-length?
       {:db (assoc db :user-addition-name (sanitise input-value))}
       {:db db}))))


#_(re-frame/reg-event-fx
 :update-user-addition-email-address
 [interceptors/validate-db]
 (fn [{:keys [db]} [_ input-value]]
   (let [valid-length? (<= (count input-value) 250)
         sanitise #(string/replace % #"\n|\r| " "")]
     (if valid-length?
       {:db (assoc db :user-addition-email-address (sanitise input-value))}
       {:db db}))))


#_(re-frame/reg-event-fx
 :toggle-user-addition-admin-role?
 [interceptors/validate-db]
 (fn [{:keys [db]} [_]]
   {:db (update db :user-addition-admin-role? not)}))


#_(re-frame/reg-event-fx
 :add-user
 [interceptors/validate-db]
 (fn [{:keys [db]} [_ user]]
   {:api (let [id (random-uuid)]
           {:command {:add-user {:user/id id
                                 :user/email-address (:user-addition-email-address db)
                                 :user/name (:user-addition-name db)
                                 :user/roles (cond-> #{:customer}
                                               (:user-addition-admin-role? db) (conj :admin))}}
            :query {:user {:user/id id}}
            :on-response :core/todo
            :on-error :core/todo
            :delay (timeout 1000)})
    :db (dissoc db
                :user-addition-email-address
                :user-addition-name
                :user-addition-admin-role?)}))
