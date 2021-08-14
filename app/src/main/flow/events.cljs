(ns flow.events
  (:require [re-frame.core :as re-frame]
            [flow.interceptors :as interceptors]
            [flow.utils :as u]
            [clojure.string :as string]))


;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;
;;;;;;;;;;;;;;;;;;;;;; Router flow ;;;;;;;;;;;;;;;;;;;;;;;
;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;

(re-frame/reg-event-fx
 :router/initialisation-started
 [interceptors/validate-db]
 (fn [{:keys [db]} event]
   {:router {}
    :db (assoc-in db [:flows :router :status] :initialising)}))


(re-frame/reg-event-fx
 :router/initialisation-ended
 [interceptors/validate-db]
 (fn [{:keys [db]} event]
   {:db (assoc-in db [:flows :router :status] :initialised)}))


(re-frame/reg-event-fx
 :router/updated
 [interceptors/validate-db]
 (fn [{:keys [db]} [_ {:keys [route route-params query-params]}]]
   (let [db (-> db
                (assoc-in [:flows :router :route] route)
                (assoc-in [:flows :router :route-params] route-params)
                (assoc-in [:flows :router :query-params] query-params))]
     (case route
       :home {:db (assoc-in db [:flows :home-page :status] :uninitialised)
              :dispatch [:home-page/initialisation-started]}
       :admin {:db db}
       :admin.users {:db db
                     :api {:query {:users {}}
                           :on-response :todo/todo
                           :on-error :todo/todo}}
       :admin.authorisations {:db db
                              :api {:query {:authorisations {}}
                                    :on-response :todo/todo
                                    :on-error :todo/todo}}
       :unknown {:db (assoc-in db [:flows :unknown-page :status] :uninitialised)
                 :dispatch [:unknown-page/initialisation-started]}))))


;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;
;;;;;;;;;;;;;;;;;;;; Home page flow ;;;;;;;;;;;;;;;;;;;;;;
;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;

(re-frame/reg-event-fx
 :home-page/initialisation-started
 [interceptors/validate-db]
 (fn [{:keys [db]} [_]]
   {:api {:query {:current-user {}}
          :on-response :home-page/initialisation-ended
          :on-error :home-page/initialisation-error-occurred}
    :db (assoc-in db [:flows :home-page :status] :initialising)}))


(re-frame/reg-event-fx
 :home-page/initialisation-ended
 [interceptors/validate-db]
 (fn [{:keys [db]} [_ {:keys [users session]}]]
   {:db (-> db
            (assoc-in [:flows :home-page :status] :initialised)
            (assoc-in [:flows :home-page :current-user-id] (:current-user-id session))
            ;; TODO - what if we're authorised? Do we really need this?
            (assoc-in [:flows :authorisation-attempt :status] :uninitialised)
            (update-in [:entities :users] merge users))}))


(re-frame/reg-event-fx
 :home-page/initialisation-error-occurred
 [interceptors/validate-db]
 (fn [{:keys [db]} [_ response]]
   {:db (-> db
            (assoc-in [:flows :home-page :status] :initialisation-errored))}))


;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;
;;;;;;;;;;;;;;;;;; Unknown page flow ;;;;;;;;;;;;;;;;;;;;;
;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;

(re-frame/reg-event-fx
 :unknown-page/initialisation-started
 [interceptors/validate-db]
 (fn [{:keys [db]} [_]]
   {:db (assoc-in db [:flows :unknown-page :status] :initialised)}))


(re-frame/reg-event-fx
 :unknown-page/route-home
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
   ;; TODO - Throw an exception if this event is called without the
   ;; right things being in place, protects from accidentally calling
   ;; the event without using the appropriate subs
   {:api {:command {:initialise-authorisation-attempt
                    (-> db
                        (get-in [:flows :authorisation-attempt])
                        (select-keys [:user/email-address]))}
          :on-response :authorisation-attempt/initialisation-ended
          :on-error :authorisation-attempt/initialisation-error-occurred}
    :db (assoc-in db [:flows :authorisation-attempt :status] :initialising)}))


(re-frame/reg-event-fx
 :authorisation-attempt/initialisation-ended
 [interceptors/validate-db]
 (fn [{:keys [db]} [_ response]]
   (js/console.warn "SUCCESS RESPONSE FROM AUTH ATTEMPT INITIALISE")
   ;; successfully-initialised
   {:db (assoc-in db [:flows :authorisation-attempt :status] :initialised)}))


(re-frame/reg-event-fx
 :authorisation-attempt/initialisation-error-occurred
 [interceptors/validate-db]
 (fn [{:keys [db]} [_ _]]
   (js/console.warn "ERROR RESPONSE FROM AUTH ATTEMPT INITIALISE")
   {:db (assoc-in db [:flows :authorisation-attempt :status] :initialisation-errored)}))


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
   ;; TODO - Throw an exception if this event is called without the
   ;; right things being in place, protects from accidentally calling
   ;; the event without using the appropriate subs
   {:api {:command {:finalise-authorisation-attempt
                    (-> db
                        (get-in [:flows :authorisation-attempt])
                        (select-keys [:user/email-address :authorisation/phrase]))}
          :query {:current-user {}}
          :on-response :authorisation-attempt/finalisation-ended
          :on-error :authorisation-attempt/finalisation-error-occurred}
    :db (assoc-in db [:flows :authorisation-attempt :status] :finalising)}))


(re-frame/reg-event-fx
 :authorisation-attempt/finalisation-ended
 [interceptors/validate-db]
 (fn [{:keys [db]} [_ {:keys [users session]}]]
   (js/console.warn "SUCCESS RESPONSE FROM AUTH ATTEMPT FINALISE")
   (if (:current-user-id session)
     {:db (-> db
              (assoc-in [:flows :authorisation-attempt :status] :successfully-finalised)
              (update :flows dissoc :authorisation-attempt)
              (assoc-in [:flows :home-page :current-user-id] (:current-user-id session))
              (update-in [:entities :users] merge users))}
     {:db (assoc-in db [:flows :authorisation-attempt :status] :unsuccessfully-finalised)})))


(re-frame/reg-event-fx
 :authorisation-attempt/finalisation-error-occurred
 [interceptors/validate-db]
 (fn [{:keys [db]} [_ _]]
   (js/console.warn "ERROR RESPONSE FROM AUTH ATTEMPT FINALISE")
   {:db (assoc-in db [:authorisation-attempt :status] :finalisation-errored)}))


;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;
;;;;;;;;;;;;;;; Deauthorisation workflow ;;;;;;;;;;;;;;;;;
;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;

#_(re-frame/reg-event-fx
 :deauthorise
 [interceptors/validate-db]
 (fn [{:keys [db]} [_]]
   {:api {:command {:deauthorise {}}
          :on-response :todo/todo
          :on-error :todo/todo}
    :db (dissoc db
                :session ;; TODO - only make this strip out the current user id
                :authorisation-email-address
                :authorisation-phrase
                :authorisation-initialised?
                :authorisation-finalised?
                :authorisation-failed?)}))


#_(re-frame/reg-event-fx
 :delete-user
 [interceptors/validate-db]
 (fn [{:keys [db]} [_ user-id]]
   {:api {:command {:delete-user {:user/id user-id}}
          :query {:user {:user/id user-id}}
          :on-response :core/todo
          :on-error :core/todo}
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
            :on-error :core/todo})
    :db (dissoc db
                :user-addition-email-address
                :user-addition-name
                :user-addition-admin-role?)}))
