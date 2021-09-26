(ns flow.events
  (:require [re-frame.core :as re-frame]
            [flow.interceptors :as interceptors]
            [flow.utils :as u]
            [clojure.string :as string]
            [cljs.core.async :refer [timeout]]))


;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;
;;;;;;;;;;;;;;;;;;;;;; Router flow ;;;;;;;;;;;;;;;;;;;;;;;
;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;

(re-frame/reg-event-fx
 :router/initialisation-started
 [interceptors/validate-db]
 (fn [{:keys [db]} event]
   {:router {}
    :db (assoc-in db [:flows :router :status] :initialisation-pending)}))


(re-frame/reg-event-fx
 :router/initialisation-ended
 [interceptors/validate-db]
 (fn [{:keys [db]} event]
   {:db (assoc-in db [:flows :router :status] :initialisation-successful)}))


(re-frame/reg-event-fx
 :router/updated
 [interceptors/validate-db]
 (fn [{:keys [db]} [_ {:keys [route route-params query-params]}]]
   (let [db (-> db
                (assoc-in [:flows :router :route] route)
                (assoc-in [:flows :router :route-params] route-params)
                (assoc-in [:flows :router :query-params] query-params))]
     (case route
       :home {:db (assoc-in db [:flows :home-page :status] :idle)
              :dispatch [:home-page/initialisation-started]}
       :admin {:db db}
       :admin.users {:db db
                     :api {:query {:users {}}
                           :on-response :todo/todo
                           :on-error :todo/todo
                           :delay (timeout 1000)}}
       :admin.authorisations {:db db
                              :api {:query {:authorisations {}}
                                    :on-response :todo/todo
                                    :on-error :todo/todo
                                    :delay (timeout 1000)}}
       :unknown {:db db}))))


;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;
;;;;;;;;;;;;;;;;;;;; Home page flow ;;;;;;;;;;;;;;;;;;;;;;
;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;

(re-frame/reg-event-fx
 :home-page/initialisation-started
 [interceptors/validate-db]
 (fn [{:keys [db]} [_]]
   {:api {:query {:current-user {}}
          :on-response :home-page/initialisation-ended
          :on-error :home-page/initialisation-errored
          :delay (timeout 0)}
    :db (assoc-in db [:flows :home-page :status] :initialisation-pending)}))


(re-frame/reg-event-fx
 :home-page/initialisation-ended
 [interceptors/validate-db]
 (fn [{:keys [db]} [_ {:keys [users session]}]]
   {:db (-> db
            (assoc-in [:flows :home-page :status] :initialisation-successful)
            (assoc-in [:flows :home-page :current-user-id] (:current-user-id session))
            (assoc-in [:flows :authorisation-attempt :status] :idle)
            (assoc-in [:flows :deauthorisation :status] :idle)
            (update-in [:entities :users] merge users))}))


(re-frame/reg-event-fx
 :home-page/initialisation-errored
 [interceptors/validate-db]
 (fn [{:keys [db]} [_ response]]
   {:db (-> db
            (assoc-in [:flows :home-page :status] :initialisation-error))}))


;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;
;;;;;;;;;;;;;;;;;; Unknown page flow ;;;;;;;;;;;;;;;;;;;;;
;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;

(re-frame/reg-event-fx
 :unknown-page/route-update-requested
 [interceptors/validate-db]
 (fn [{:keys [db]} [_]]
   {:router {:route :home}
    :db (update db :flows dissoc :unknown-page)}))


;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;
;;;;;;;;;;;;;;; Authorisation attempt flow ;;;;;;;;;;;;;;;
;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;

(re-frame/reg-event-fx
 :authorisation-attempt/email-address-updated
 [interceptors/validate-db]
 (fn [{:keys [db]} [_ input-value]]
   (let [valid-length? (<= (count input-value) 250)
         sanitise #(string/replace % #"\n|\r| " "")]
     (if valid-length?
       {:db (assoc-in db
                      [:flows :authorisation-attempt :user/email-address]
                      (sanitise input-value))}
       {:db db}))))


(re-frame/reg-event-fx
 :authorisation-attempt/initialisation-started
 [interceptors/validate-db]
 (fn [{:keys [db]} [_]]
   {:api {:command {:initialise-authorisation-attempt
                    (-> db
                        (get-in [:flows :authorisation-attempt])
                        (select-keys [:user/email-address]))}
          :on-response :authorisation-attempt/initialisation-ended
          :on-error :authorisation-attempt/initialisation-errored
          :delay (timeout 1000)}
    :db (assoc-in db [:flows :authorisation-attempt :status] :initialisation-pending)}))


(re-frame/reg-event-fx
 :authorisation-attempt/initialisation-ended
 [interceptors/validate-db]
 (fn [{:keys [db]} [_ response]]
   {:db (assoc-in db [:flows :authorisation-attempt :status] :initialisation-successful)}))


(re-frame/reg-event-fx
 :authorisation-attempt/initialisation-errored
 [interceptors/validate-db]
 (fn [{:keys [db]} [_ _]]
   {:db (assoc-in db [:flows :authorisation-attempt :status] :initialisation-error)}))


(re-frame/reg-event-fx
 :authorisation-attempt/phrase-updated
 [interceptors/validate-db]
 (fn [{:keys [db]} [_ input-value]]
   (let [valid-length? (<= (count input-value) 250)
         sanitise #(string/replace % #"\n|\r| " "")]
     (if valid-length?
       {:db (assoc-in db
                      [:flows :authorisation-attempt :authorisation/phrase]
                      (sanitise input-value))}
       {:db db}))))


(re-frame/reg-event-fx
 :authorisation-attempt/finalisation-started
 [interceptors/validate-db]
 (fn [{:keys [db]} [_]]
   {:api {:command {:finalise-authorisation-attempt
                    (-> db
                        (get-in [:flows :authorisation-attempt])
                        (select-keys [:user/email-address :authorisation/phrase]))}
          :query {:current-user {}}
          :on-response :authorisation-attempt/finalisation-ended
          :on-error :authorisation-attempt/finalisation-errored
          :delay (timeout 1000)}
    :db (assoc-in db [:flows :authorisation-attempt :status] :finalisation-pending)}))


(re-frame/reg-event-fx
 :authorisation-attempt/finalisation-ended
 [interceptors/validate-db]
 (fn [{:keys [db]} [_ {:keys [users session]}]]
   (if (:current-user-id session)
     {:db (-> db
              (assoc-in [:flows :authorisation-attempt :status] :finalisation-successful)
              (assoc-in [:flows :deauthorisation :status] :idle)
              (assoc-in [:flows :home-page :current-user-id] (:current-user-id session))
              (update-in [:entities :users] merge users))}
     {:db (assoc-in db [:flows :authorisation-attempt :status] :finalisation-unsuccessful)})))


(re-frame/reg-event-fx
 :authorisation-attempt/finalisation-errored
 [interceptors/validate-db]
 (fn [{:keys [db]} [_ _]]
   {:db (assoc-in db [:authorisation-attempt :status] :finalisation-error)}))


;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;
;;;;;;;;;;;;;;;;; Deauthorisation flow ;;;;;;;;;;;;;;;;;;;
;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;

(re-frame/reg-event-fx
 :deauthorisation/started
 [interceptors/validate-db]
 (fn [{:keys [db]} [_]]
   {:api {:command {:deauthorise {}}
          :on-response :deauthorisation/ended
          :on-error :deauthorisation/errored
          :delay (timeout 1000)}
    :db (assoc-in db [:flows :deauthorisation :status] :pending)}))


(re-frame/reg-event-fx
 :deauthorisation/ended
 [interceptors/validate-db]
 (fn [{:keys [db]} [_]]
   {:db (-> db
            (assoc-in [:flows :deauthorisation :status] :successful)
            (assoc-in [:flows :authorisation-attempt :status] :idle)
            (update-in [:flows :authorisation-attempt] dissoc :user/email-address)
            (update-in [:flows :authorisation-attempt] dissoc :authorisation/phrase)
            (assoc-in [:flows :home-page :current-user-id] nil))}))


(re-frame/reg-event-fx
 :deauthorisation/errored
 [interceptors/validate-db]
 (fn [{:keys [db]} [_]]
   {:db (assoc-in db [:flows :deauthorisation :status] :error)}))


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
