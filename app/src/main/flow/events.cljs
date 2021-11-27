(ns flow.events
  (:require [re-frame.core :as re-frame]
            [flow.interceptors :as interceptors]
            [flow.utils :as u]
            [clojure.string :as string]))


;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;
;;;;;;;;;;;;;;;;;;;;;;; App flow ;;;;;;;;;;;;;;;;;;;;;;;;;
;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;

(re-frame/reg-event-fx
 :app/route
 [interceptors/validate-db]
 (fn [{:keys [db]} [_ route]]
   (let [key [:views :app]]
     {:router {:route route}})))


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
 :pages.home/start-initialisation
 [interceptors/validate-db]
 (fn [{:keys [db]} [_ {:keys [route-params query-params]}]]
   (let [key [:views :app]]
     {:api {:query {:current-user {}}
            :on-response [:pages.home/end-initialisation]
            :on-error [:app/error]
            :delay 1000}
      :db (update-in db key assoc :status :routing)})))


(re-frame/reg-event-fx
 :pages.home/end-initialisation
 [interceptors/validate-db]
 (fn [{:keys [db]} [_ {:keys [users session]}]]
   {:db (-> db
            ;; TODO - why should an event in this ns care about all this?
            (assoc-in [:views :app :status] :idle)
            (assoc-in [:views :app :routing :route] :home)
            (assoc-in [:views :app :routing :route-params] nil)
            (assoc-in [:views :app :routing :query-params] nil)
            (assoc-in [:views :app :session] session)
            ;; TODO - could this be handled elsewhere?
            (assoc-in [:views :app :views :pages.home :views :authorisation-attempt :status] :idle)
            (assoc-in [:views :app :views :pages.home :views :authorisation-attempt :email-address] "")
            (assoc-in [:views :app :views :pages.home :views :authorisation-attempt :phrase] "")
            (assoc-in [:views :app :views :pages.home :views :deauthorisation :status] :idle)
            (update-in [:entities :users] merge users))}))


;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;
;;;;;;;;;;;;;;; Admin users page flow ;;;;;;;;;;;;;;;;;;;;
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
    :db (assoc-in db [:views :app :status] :routing)}))


(re-frame/reg-event-fx
 :pages.admin.users/end-initialisation
 [interceptors/validate-db]
 (fn [{:keys [db]} [_ {:keys [users session metadata]}]]
   (let [key [:views :app :views :pages.admin.users]]
     {:db (-> db
              (update-in key assoc :paging-offset (get-in metadata [:users :next-offset]))
              (update-in key assoc :paging-exhausted? (get-in metadata [:users :exhausted?]))
              (assoc-in [:views :app :status] :idle)
              (assoc-in [:views :app :routing :route] :admin.users)
              (assoc-in [:views :app :routing :route-params] nil)
              (assoc-in [:views :app :routing :query-params] nil)
              (assoc-in [:views :app :session] session)
              (update-in [:entities :users] merge users)
              ;; TODO - could this be handled elsewhere?
              (assoc-in [:views :app :views :pages.admin.users :views :user-addition :status] :idle)
              (assoc-in [:views :app :views :pages.admin.users :views :user-addition :name] "")
              (assoc-in [:views :app :views :pages.admin.users :views :user-addition :email-address] "")
              (assoc-in [:views :app :views :pages.admin.users :views :user-addition :roles] #{:customer}))})))


(re-frame/reg-event-fx
 :pages.admin.users/start-paging
 [interceptors/validate-db]
 (fn [{:keys [db]} [_]]
   (let [key [:views :app :views :pages.admin.users]
         context (get-in db key)]
     {:api {:query {:users {}}
            :metadata {:users {:limit 2 :offset (:paging-offset context)}}
            :on-response [:pages.admin.users/end-paging]
            :on-error [:app/error]
            :delay 1000}
      :db (update-in db key assoc :status :paging-pending)})) )


(re-frame/reg-event-fx
 :pages.admin.users/end-paging
 [interceptors/validate-db]
 (fn [{:keys [db]} [_ {:keys [users metadata]}]]
   (let [key [:views :app :views :pages.admin.users]]
     {:db (-> db
              (update-in key assoc :paging-offset (get-in metadata [:users :next-offset]))
              (update-in key assoc :paging-exhausted? (get-in metadata [:users :exhausted?]))
              ;; TODO - this status is a complete conflation of the page's status
              (update-in key assoc :status :paging-successful)
              (update-in [:entities :users] merge users))})))



;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;
;;;;;;;;; Admin authorisations page flow ;;;;;;;;;;;;;;;;;
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
    :db (assoc-in db [:views :app :status] :routing)}))


(re-frame/reg-event-fx
 :pages.admin.authorisations/end-initialisation
 [interceptors/validate-db]
 (fn [{:keys [db]} [_ {:keys [users authorisations metadata session]}]]
   (let [key [:views :app :views :pages.admin.authorisations]]
     {:db (-> db
              (update-in key assoc :paging-offset (get-in metadata [:authorisations :next-offset]))
              (update-in key assoc :paging-exhausted? (get-in metadata [:authorisations :exhausted?]))
              (assoc-in [:views :app :status] :idle)
              (assoc-in [:views :app :routing :route] :admin.authorisations)
              (assoc-in [:views :app :routing :route-params] nil)
              (assoc-in [:views :app :routing :query-params] nil)
              (assoc-in [:views :app :session] session)
              (update-in [:entities :users] merge users)
              (update-in [:entities :authorisations] merge authorisations))})))


(re-frame/reg-event-fx
 :pages.admin.authorisations/start-paging
 [interceptors/validate-db]
 (fn [{:keys [db]} [_]]
   (let [key [:views :app :views :pages.admin.authorisations]
         context (get-in db key)]
     {:api {:query {:authorisations {}}
            :metadata {:authorisations {:limit 2 :offset (:paging-offset context)}}
            :on-response [:pages.admin.authorisations/end-paging]
            :on-error [:app/error]
            :delay 1000}
      :db (update-in db key assoc :status :paging-pending)})) )


(re-frame/reg-event-fx
 :pages.admin.authorisations/end-paging
 [interceptors/validate-db]
 (fn [{:keys [db]} [_ {:keys [authorisations metadata]}]]
   (let [key [:views :app :views :pages.admin.authorisations]]
     {:db (-> db
              (update-in key assoc :paging-offset (get-in metadata [:authorisations :next-offset]))
              (update-in key assoc :paging-exhausted? (get-in metadata [:authorisations :exhausted?]))
              (update-in key assoc :status :paging-successful)
              (update-in [:entities :authorisations] merge authorisations))})))



;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;
;;;;;;;;;;;;;;;;;; Unknown page flow ;;;;;;;;;;;;;;;;;;;;;
;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;

(re-frame/reg-event-fx
 :pages.unknown/start-initialisation
 [interceptors/validate-db]
 (fn [{:keys [db]} [_ {:keys [route-params query-params]}]]
   {:api {:query {:current-user {}}
          :on-response [:pages.unknown/end-initialisation]
          :on-error [:app/error]
          :delay 1000}
    :db (assoc-in db [:views :app :status] :routing)}))


(re-frame/reg-event-fx
 :pages.unknown/end-initialisation
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
   (let [key [:views :app :views :pages.home :views :authorisation-attempt]]
     {:db (update-in db key assoc :email-address (->> value (u/constrain-string 250) u/sanitise-string))})))


(re-frame/reg-event-fx
 :authorisation-attempt/start-initialisation
 [interceptors/validate-db]
 (fn [{:keys [db]} [_]]
   (let [key [:views :app :views :pages.home :views :authorisation-attempt]
         context (get-in db key)]
     {:api {:command {:initialise-authorisation-attempt
                      {:user/email-address (:email-address context)}}
            :on-response [:authorisation-attempt/end-initialisation]
            :on-error [:app/error]
            :delay 1000}
      :db (update-in db key assoc :status :initialisation-pending)})))


(re-frame/reg-event-fx
 :authorisation-attempt/end-initialisation
 [interceptors/validate-db]
 (fn [{:keys [db]} [_ response]]
   (let [key [:views :app :views :pages.home :views :authorisation-attempt]]
     {:db (update-in db key assoc :status :initialisation-successful)})))


(re-frame/reg-event-fx
 :authorisation-attempt/update-phrase
 [interceptors/validate-db]
 (fn [{:keys [db]} [_ value]]
   (let [key [:views :app :views :pages.home :views :authorisation-attempt]]
     {:db (update-in db key assoc :phrase (->> value (u/constrain-string 250) u/sanitise-string))})))


(re-frame/reg-event-fx
 :authorisation-attempt/start-finalisation
 [interceptors/validate-db]
 (fn [{:keys [db]} [_]]
   (let [key [:views :app :views :pages.home :views :authorisation-attempt]
         context (get-in db key)]
     {:api {:command {:finalise-authorisation-attempt
                      {:user/email-address (:email-address context)
                       :authorisation/phrase (:phrase context)}}
            :query {:current-user {}}
            :on-response [:authorisation-attempt/end-finalisation]
            ;; TODO - where should this knowledge come from?
            :on-error [:app/error]
            :delay 1000}
      :db (update-in db key assoc :status :finalisation-pending)})))


(re-frame/reg-event-fx
 :authorisation-attempt/end-finalisation
 [interceptors/validate-db]
 (fn [{:keys [db]} [_ {:keys [users session]}]]
   (let [key [:views :app :views :pages.home :views :authorisation-attempt]]
     (if (empty? users)
       {:db (update-in db key assoc :status :finalisation-unsuccessful)}
       {:db (-> db
                (update-in key assoc :status :finalisation-successful)
                (update-in key assoc :email-address "")
                (update-in key assoc :phrase "")
                ;; TODO - where should this knowledge come from?
                (assoc-in [:views :app :views :pages.home :views :deauthorisation] {:status :idle})
                (assoc-in [:views :app :session] session)
                (update-in [:entities :users] merge users))}))))



;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;
;;;;;;;;;;;;;;;;; Deauthorisation flow ;;;;;;;;;;;;;;;;;;;
;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;

(re-frame/reg-event-fx
 :deauthorisation/start
 [interceptors/validate-db]
 (fn [{:keys [db]} [_]]
   (let [key [:views :app :views :pages.home :views :deauthorisation]
         context (get-in db key)]
     {:api {:command {:deauthorise {}}
            :query {:current-user {}}
            :on-response [:deauthorisation/end]
            ;; TODO - where should this knowledge come from?
            :on-error [:app/error]
            :delay 1000}
      :db (update-in db key assoc :status :pending)})))


(re-frame/reg-event-fx
 :deauthorisation/end
 [interceptors/validate-db]
 (fn [{:keys [db]} [_ {:keys [users session]}]]
   (let [key [:views :app :views :pages.home :views :deauthorisation]]
     (if (empty? users)
       {:db (-> db
                (update-in key assoc :status :successful)
                (assoc-in [:views :app :session] session)
                ;; TODO - where should this knowledge come from?
                (assoc-in [:views :app :views :pages.home :views :authorisation-attempt] {:status :idle})
                (assoc-in [:views :app :views :pages.home :views :authorisation-attempt :email-address] "")
                (assoc-in [:views :app :views :pages.home :views :authorisation-attempt :phrase] "")
                (assoc-in [:entities] {}))}
       {:db (update-in db key assoc :status :error)}))))



;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;
;;;;;;;;;;;;;;;;; User addition flow ;;;;;;;;;;;;;;;;;;;;;
;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;

(re-frame/reg-event-fx
 :user-addition/update-email-address
 [interceptors/validate-db]
 (fn [{:keys [db]} [_ value]]
   (let [key [:views :app :views :pages.admin.users :views :user-addition]]
     {:db (update-in db key assoc :email-address (->> value (u/constrain-string 250) u/sanitise-string))})))


(re-frame/reg-event-fx
 :user-addition/update-name
 [interceptors/validate-db]
 (fn [{:keys [db]} [_ value]]
   (let [key [:views :app :views :pages.admin.users :views :user-addition]]
     {:db (update-in db key assoc :name (->> value (u/constrain-string 250) u/sanitise-string))})))


(re-frame/reg-event-fx
 :user-addition/toggle-admin-role
 [interceptors/validate-db]
 (fn [{:keys [db]} [_]]
   (let [key [:views :app :views :pages.admin.users :views :user-addition]
         admin-role? (contains? (:roles (get-in db key)) :admin)]
     {:db (update-in db key assoc :roles (if admin-role? #{:customer} #{:customer :admin}))})))


(re-frame/reg-event-fx
 :user-addition/start
 [interceptors/validate-db]
 (fn [{:keys [db]} [_]]
   (let [id (random-uuid)
         key [:views :app :views :pages.admin.users :views :user-addition]
         context (get-in db key)]
     {:api {:command {:add-user {:user/id id
                                 :user/email-address (:email-address context)
                                 :user/name (:name context)
                                 :user/roles (:roles context)}}
            :query {:user {:user/id id}}
            :on-response [:user-addition/end]
            :on-error [:app/error]
            :delay 1000}
      :db (update-in db key assoc :status :pending)})))


(re-frame/reg-event-fx
 :user-addition/end
 [interceptors/validate-db]
 (fn [{:keys [db]} [_ {:keys [users]}]]
   (let [key [:views :app :views :pages.admin.users :views :user-addition]
         context (get-in db key)]
     (if (empty? users)
       {:db (update-in db key assoc :status :unsuccessful)}
       {:db (-> db
                (update-in key assoc :status :successful)
                (update-in key assoc :name "")
                (update-in key assoc :email-address "")
                (update-in key assoc :roles #{:customer})
                (update-in [:entities :users] merge users))}))))


;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;
;;;;;;;;;;;;;;;;;;;;;;; User flow ;;;;;;;;;;;;;;;;;;;;;;;;
;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;

(re-frame/reg-event-fx
 :user/start-deletion
 [interceptors/validate-db]
 (fn [{:keys [db]} [_ id]]
   (let [key [:views :app :views :pages.admin.users :views :user id]]
     {:api {:command {:delete-user {:user/id id}}
            :query {:user {:user/id id}}
            :on-response [:user/end-deletion id]
            :on-error [:app/error]
            :delay 1000}
      :db (update-in db key assoc :status :deletion-pending)})))


(re-frame/reg-event-fx
 :user/end-deletion
 [interceptors/validate-db]
 (fn [{:keys [db]} [_ id {:keys [users]}]]
   (let [key [:views :app :views :pages.admin.users :views :user id]]
     {:db (-> db
              (update-in key dissoc :status)
              (update-in [:entities :users] merge users))})))
