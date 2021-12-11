(ns flow.events
  (:require [re-frame.core :as re-frame]
            [flow.interceptors :as interceptors]
            [flow.utils :as u]
            [clojure.string :as string]))


;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;
;;;;;;;;;;;;;;;;;;;;;;; App ;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;
;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;

(re-frame/reg-event-fx
 :app/route
 [interceptors/validate-db]
 (fn [{:keys [db]} [_ route]]
   {:router {:route route}}))


(re-frame/reg-event-fx
 :app/error
 [interceptors/validate-db]
 (fn [{:keys [db]} [_]]
   {:db (assoc-in db [:notifier :error?] true)}))



;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;
;;;;;;;;;;;;;;;;;;;; Home page ;;;;;;;;;;;;;;;;;;;;;;;;;;;
;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;

(re-frame/reg-event-fx
 :pages.home/start-initialisation
 [interceptors/validate-db]
 (fn [{:keys [db]} [_ {:keys [route-params query-params]}]]
   {:api {:query {:current-user {}}
          :on-response [:pages.home/end-initialisation]
          :on-error [:app/error]
          :delay 1000}
    :db (assoc-in db [:router :routing?] true)}))


(re-frame/reg-event-fx
 :pages.home/end-initialisation
 [interceptors/validate-db]
 (fn [{:keys [db]} [_ {:keys [users authorisations session]}]]
   (let [key [:views :app :views :pages.home]]
     {:db (-> db
              (assoc :router
                     {:routing? false
                      :route :home
                      :route-params nil
                      :query-params nil})
              (assoc :session session)
              (assoc-in [:views :app :views] {})
              (assoc-in [:entities :users] users)
              (assoc-in [:entities :authorisations] authorisations)
              ;; TODO - These are key dependent but relate to child views, can it be done elsewhere?
              (update-in key assoc-in [:views :authorisation]
                         {:status :idle
                          :email-address ""
                          :phrase ""})
              (update-in key assoc-in [:views :deauthorisation]
                         {:status :idle}))})))


;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;
;;;;;;;;;;;;;;; Admin users page ;;;;;;;;;;;;;;;;;;;;;;;;;
;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;

(re-frame/reg-event-fx
 :pages.admin.users/start-initialisation
 [interceptors/validate-db]
 (fn [{:keys [db]} [_ {:keys [route-params query-params]}]]
   {:api {:query {:current-user {}
                  :users {}}
          :metadata {:users {:limit 2 :offset nil}}
          :on-response [:pages.admin.users/end-initialisation]
          :on-error [:app/error]
          :delay 1000}
    :db (assoc-in db [:router :routing?] true)}))


(re-frame/reg-event-fx
 :pages.admin.users/end-initialisation
 [interceptors/validate-db]
 (fn [{:keys [db]} [_ {:keys [users authorisations metadata session]}]]
   (let [key [:views :app :views :pages.admin.users]]
     {:db (-> db
              (assoc :router
                     {:routing? false
                      :route :admin.users
                      :route-params nil
                      :query-params nil})
              (assoc :session session)
              (assoc-in [:views :app :views] {})
              (assoc-in [:entities :users] users)
              (assoc-in [:entities :authorisations] authorisations)
              ;; TODO - These are key dependent but relate to child views, can it be done elsewhere?
              (update-in key assoc-in [:views :user-addition]
                         {:status :idle
                          :email-address ""
                          :roles #{:customer}})
              (update-in key assoc-in [:views :pagination]
                         {:status :idle
                          :offset (get-in metadata [:users :next-offset])
                          :exhausted? (get-in metadata [:users :exhausted?])}))})))



;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;
;;;;;;;;; Admin authorisations page ;;;;;;;;;;;;;;;;;;;;;;
;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;

(re-frame/reg-event-fx
 :pages.admin.authorisations/start-initialisation
 [interceptors/validate-db]
 (fn [{:keys [db]} [_ {:keys [route-params query-params]}]]
   {:api {:query {:current-user {}
                  :authorisations {}}
          :metadata {:authorisations {:limit 2 :offset nil}}
          :on-response [:pages.admin.authorisations/end-initialisation]
          :on-error [:app/error]
          :delay 1000}
    :db (assoc-in db [:router :routing] true)}))


(re-frame/reg-event-fx
 :pages.admin.authorisations/end-initialisation
 [interceptors/validate-db]
 (fn [{:keys [db]} [_ {:keys [users authorisations metadata session]}]]
   (let [key [:views :app :views :pages.admin.authorisations]]
     {:db (-> db
              (assoc :router
                     {:routing? false
                      :route :admin.authoirsations
                      :route-params nil
                      :query-params nil})
              (assoc :session session)
              (assoc-in [:views :app :views] {})
              (assoc-in [:entities :users] users)
              (assoc-in [:entities :authorisations] authorisations)
              ;; TODO - These are key dependent but relate to child views, can it be done elsewhere?
              (update-in key assoc-in [:views :pagination]
                         {:status :idle
                          :offset (get-in metadata [:authorisations :next-offset])
                          :exhausted? (get-in metadata [:authorisations :exhausted?])}))})))



;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;
;;;;;;;;;;;;;;;;;; Unknown page ;;;;;;;;;;;;;;;;;;;;;;;;;;
;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;

(re-frame/reg-event-fx
 :pages.unknown/start-initialisation
 [interceptors/validate-db]
 (fn [{:keys [db]} [_ {:keys [route-params query-params]}]]
   {:api {:query {:current-user {}}
          :on-response [:pages.unknown/end-initialisation]
          :on-error [:app/error]
          :delay 1000}
    :db (assoc-in db [:router :routing?] true)}))


(re-frame/reg-event-fx
 :pages.unknown/end-initialisation
 [interceptors/validate-db]
 (fn [{:keys [db]} [_ {:keys [users authorisations session]}]]
   {:db (-> db
            (assoc :router
                   {:routing? false
                    :route :unknown
                    :route-params nil
                    :query-params nil})
            (assoc :session session)
            (assoc-in [:views :app :views] {})
            (assoc-in [:entities :users] users)
            (assoc-in [:entities :authorisations] authorisations))}))



;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;
;;;;;;;;;;;;;;;;;;; Pagination ;;;;;;;;;;;;;;;;;;;;;;;;;;;
;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;

(re-frame/reg-event-fx
 :pagination/start
 [interceptors/validate-db]
 (fn [{:keys [db]} [_ key entity-type]]
   (let [context (get-in db key)]
     {:api {:query {entity-type {}}
            :metadata {entity-type {:limit 2 :offset (:offset context)}}
            :on-response [:pagination/end key entity-type]
            :on-error [:app/error]
            :delay 1000}
      :db (update-in db key assoc :status :pending)})) )


(re-frame/reg-event-fx
 :pagination/end
 [interceptors/validate-db]
 (fn [{:keys [db]} [_ key entity-type response]]
   {:db (-> db
            (update-in [:entities entity-type] merge (get response entity-type))
            (update-in key assoc :status :idle)
            (update-in key assoc :offset (get-in response [:metadata entity-type :next-offset]))
            (update-in key assoc :exhausted? (get-in response [:metadata entity-type :exhausted?])))}))



;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;
;;;;;;;;;;;;;;;;;; Authorisation ;;;;;;;;;;;;;;;;;;;;;;;;;
;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;

(re-frame/reg-event-fx
 :authorisation/update-email-address
 [interceptors/validate-db]
 (fn [{:keys [db]} [_ key value]]
   {:db (update-in db key assoc :email-address (->> value (u/constrain-string 250) u/sanitise-string))}))


(re-frame/reg-event-fx
 :authorisation/start-initialisation
 [interceptors/validate-db]
 (fn [{:keys [db]} [_ key]]
   (let [context (get-in db key)]
     {:api {:command {:initialise-authorisation-attempt
                      {:user/email-address (:email-address context)}}
            :on-response [:authorisation/end-initialisation key]
            :on-error [:app/error]
            :delay 1000}
      :db (update-in db key assoc :status :initialisation-pending)})))


(re-frame/reg-event-fx
 :authorisation/end-initialisation
 [interceptors/validate-db]
 (fn [{:keys [db]} [_ key _]]
   {:db (update-in db key assoc :status :initialisation-successful)}))


(re-frame/reg-event-fx
 :authorisation/update-phrase
 [interceptors/validate-db]
 (fn [{:keys [db]} [_ key value]]
   {:db (update-in db key assoc :phrase (->> value (u/constrain-string 250) u/sanitise-string))}))


(re-frame/reg-event-fx
 :authorisation/start-finalisation
 [interceptors/validate-db]
 (fn [{:keys [db]} [_ key]]
   (let [context (get-in db key)]
     {:api {:command {:finalise-authorisation-attempt
                      {:user/email-address (:email-address context)
                       :authorisation/phrase (:phrase context)}}
            :query {:current-user {}}
            :on-response [:authorisation/end-finalisation key]
            :on-error [:app/error]
            :delay 1000}
      :db (update-in db key assoc :status :finalisation-pending)})))


(re-frame/reg-event-fx
 :authorisation/end-finalisation
 [interceptors/validate-db]
 (fn [{:keys [db]} [_ key {:keys [users session]}]]
   (if (empty? users)
     {:db (update-in db key assoc :status :finalisation-unsuccessful)}
     {:db (-> db
              (assoc :session session)
              (update-in [:entities :users] merge users)
              (assoc-in key {:status :idle :email-address "" :phrase ""}))})))



;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;
;;;;;;;;;;;;;;;;; Deauthorisation ;;;;;;;;;;;;;;;;;;;;;;;;
;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;

(re-frame/reg-event-fx
 :deauthorisation/start
 [interceptors/validate-db]
 (fn [{:keys [db]} [_ key]]
   {:api {:command {:deauthorise {}}
          :query {:current-user {}}
          :on-response [:deauthorisation/end key]
          :on-error [:app/error]
          :delay 1000}
    :db (update-in db key assoc :status :pending)}))


(re-frame/reg-event-fx
 :deauthorisation/end
 [interceptors/validate-db]
 (fn [{:keys [db]} [_ key {:keys [users session]}]]
   (if (empty? users)
     {:db (-> db
              (assoc :session session)
              (assoc-in [:entities] {})
              (assoc-in key {:status :idle}))}
     {:db (update-in db key assoc :status :unsuccessful)})))



;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;
;;;;;;;;;;;;;;;;; User addition ;;;;;;;;;;;;;;;;;;;;;;;;;;
;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;

(re-frame/reg-event-fx
 :user-addition/update-email-address
 [interceptors/validate-db]
 (fn [{:keys [db]} [_ key value]]
   {:db (update-in db key assoc :email-address (->> value (u/constrain-string 250) u/sanitise-string))}))


(re-frame/reg-event-fx
 :user-addition/update-name
 [interceptors/validate-db]
 (fn [{:keys [db]} [_ key value]]
   {:db (update-in db key assoc :name (->> value (u/constrain-string 250) u/sanitise-string))}))


(re-frame/reg-event-fx
 :user-addition/toggle-admin-role
 [interceptors/validate-db]
 (fn [{:keys [db]} [_ key]]
   (let [admin-role? (contains? (:roles (get-in db key)) :admin)]
     {:db (update-in db key assoc :roles (if admin-role? #{:customer} #{:customer :admin}))})))


(re-frame/reg-event-fx
 :user-addition/start
 [interceptors/validate-db]
 (fn [{:keys [db]} [_ key]]
   (let [id (random-uuid)
         context (get-in db key)]
     {:api {:command {:add-user {:user/id id
                                 :user/email-address (:email-address context)
                                 :user/name (:name context)
                                 :user/roles (:roles context)}}
            :query {:user {:user/id id}}
            :on-response [:user-addition/end key]
            :on-error [:app/error]
            :delay 1000}
      :db (update-in db key assoc :status :pending)})))


(re-frame/reg-event-fx
 :user-addition/end
 [interceptors/validate-db]
 (fn [{:keys [db]} [_ key {:keys [users]}]]
   (let [context (get-in db key)]
     (if (empty? users)
       {:db (update-in db key assoc :status :unsuccessful)}
       {:db (-> db
                (update-in [:entities :users] merge users)
                (assoc-in key {:status :successful
                               :name ""
                               :email-address ""
                               :roles #{:customer}}))}))))


;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;
;;;;;;;;;;;;;;;;; User deletion ;;;;;;;;;;;;;;;;;;;;;;;;;;
;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;

(re-frame/reg-event-fx
 :user-deletion/start
 []
 (fn [{:keys [db]} [_ key id]]
   {:api {:command {:delete-user {:user/id id}}
          :query {:user {:user/id id}}
          :on-response [:user-deletion/end key]
          :on-error [:app/error]
          :delay 1000}
    :db (assoc-in db key {:status :pending})}))


(re-frame/reg-event-fx
 :user-deletion/end
 [interceptors/validate-db]
 (fn [{:keys [db]} [_ key {:keys [users]}]]
   {:db (-> db
            (update-in [:entities :users] merge users)
            (assoc-in key {:status :idle}))}))
