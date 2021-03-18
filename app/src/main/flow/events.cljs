(ns flow.events
  (:require [re-frame.core :as re-frame]
            [flow.interceptors :as interceptors]
            [clojure.string :as string]))


(re-frame/reg-event-fx
 :initialise
 [interceptors/schema]
 (fn [{:keys [db]} event]
   {:db {}
    :initialise-routing {}
    :query {:current-user {}}}))


(re-frame/reg-event-fx
 :route
 [interceptors/schema]
 (fn [{:keys [db]} [_ {:keys [route route-params query-params]}]]
   (let [db (-> db
                (assoc :route route)
                (assoc :route-params route-params)
                (assoc :query-params query-params))]
     (case route
       :home {:db db}
       :admin {:db db}
       :admin.users {:db db
                     :query {:users {}}}
       :admin.authorisations {:db db
                              :query {:authorisations {}}}
       :unknown {:db db}
       {:db db}))))


(re-frame/reg-event-fx
 :update-route
 [interceptors/schema]
 (fn [{:keys [db]} [_ route]]
   {:update-route {:route route
                   :db db}}))


(re-frame/reg-event-fx
 :query-success
 [interceptors/schema interceptors/current-user-id]
 (fn [{:keys [db]} [_ query response]]
   (case (-> query keys first)
     :current-user {:db (update db :user merge (:users response))}
     :users {:db (update db :user merge (:users response))}
     :user {:db (update db :user merge (:users response))}
     :authorisations {:db (update db :authorisation merge (:authorisations response))}
     {:db db})))


(re-frame/reg-event-fx
 :query-failure
 [interceptors/schema interceptors/current-user-id]
 (fn [{:keys [db]} [_ query response]]
   {:db (assoc db :error? true)}))


(re-frame/reg-event-fx
 :command-success
 [interceptors/schema interceptors/current-user-id]
 (fn [{:keys [db]} [_ command response]]
   (case (-> command keys first)
     :initialise-authorisation-attempt {:db db}
     :finalise-authorisation-attempt {:db (assoc db
                                                 :authorisation-finalised? false
                                                 :authorisation-failed? false)
                                      ;; TODO - this needs to lead to the auth
                                      ;; finalise becoming true when the user
                                      ;; arrives? Or is the loading page enough?
                                      :query {:current-user {}}}
     :deauthorise {:db db}
     :add-user (let [id (get-in command [:add-user :user :user/id])
                     id-resolution (get-in response [:metadata :id-resolution])]
                 (if-let [id (id-resolution id)]
                   ;; TODO - need to do something when nothing was created
                   {:query {:user {:user/id (id-resolution id)}}
                    :db db}
                   {:db db}))
     :delete-user (let [id (get-in command [:delete-user :user/id])]
                    {:query {:user {:user/id id}}
                     :db db})
     {:db db})))


(re-frame/reg-event-fx
 :command-failure
 [interceptors/schema interceptors/current-user-id]
 (fn [{:keys [db]} [_ command response]]
   {:db (assoc db :error? true)}))


(re-frame/reg-event-fx
 :update-authorisation-email-address
 [interceptors/schema]
 (fn [{:keys [db]} [_ input-value]]
   (let [valid-length? (<= (count input-value) 250)
         sanitise #(-> %
                       (string/trim)
                       (string/trim-newline)
                       (string/replace #" " ""))]
     (if valid-length?
       {:db (assoc db :authorisation-email-address (sanitise input-value))}
       {:db db}))))


(re-frame/reg-event-fx
 :update-authorisation-phrase
 [interceptors/schema]
 (fn [{:keys [db]} [_ input-value]]
   (let [valid-length? (<= (count input-value) 250)
         sanitise #(-> %
                       (string/trim)
                       (string/trim-newline)
                       (string/replace #" " ""))]
     (if valid-length?
       {:db (assoc db :authorisation-phrase (sanitise input-value))}
       {:db db}))))


(re-frame/reg-event-fx
 ;; TODO attempt
 :initialise-authorisation
 [interceptors/schema]
 (fn [{:keys [db]} [_]]
   (let [{:keys [authorisation-initialised?]} db]
     (if authorisation-initialised?
       {:db db}
       {:command {:initialise-authorisation-attempt
                  {:user/email-address (:authorisation-email-address db)}}
        :db (assoc db :authorisation-initialised? true)}))))


(re-frame/reg-event-fx
 ;; TODO - attempt
 :finalise-authorisation
 [interceptors/schema]
 (fn [{:keys [db]} [_]]
   (let [{:keys [authorisation-finalised?]} db]
     (if authorisation-finalised?
       {:db db}
       {:command {:finalise-authorisation-attempt
                  {:user/email-address (:authorisation-email-address db)
                   :authorisation/phrase (:authorisation-phrase db)}}
        :db (assoc db :authorisation-finalised? true)}))))


(re-frame/reg-event-fx
 :deauthorise
 [interceptors/schema]
 (fn [{:keys [db]} [_]]
   {:command {:deauthorise {}}
    :db (dissoc db
                :current-user-id
                :authorisation-email-address
                :authorisation-phrase
                :authorisation-initialised?
                :authorisation-finalised?
                :authorisation-failed?)}))


(re-frame/reg-event-fx
 :delete-user
 [interceptors/schema]
 (fn [{:keys [db]} [_ user-id]]
   {:command {:delete-user {:user/id user-id}}
    :db db}))


(re-frame/reg-event-fx
 :update-user-addition-name
 [interceptors/schema]
 (fn [{:keys [db]} [_ input-value]]
   (let [valid-length? (<= (count input-value) 250)
         sanitise #(-> %
                       (string/trim)
                       (string/trim-newline)
                       (string/replace #" " ""))]
     (if valid-length?
       {:db (assoc db :user-addition-name (sanitise input-value))}
       {:db db}))))


(re-frame/reg-event-fx
 :update-user-addition-email-address
 [interceptors/schema]
 (fn [{:keys [db]} [_ input-value]]
   (let [valid-length? (<= (count input-value) 250)
         sanitise #(-> %
                       (string/trim)
                       (string/trim-newline)
                       (string/replace #" " ""))]
     (if valid-length?
       {:db (assoc db :user-addition-email-address (sanitise input-value))}
       {:db db}))))


(re-frame/reg-event-fx
 :toggle-user-addition-admin-role?
 [interceptors/schema]
 (fn [{:keys [db]} [_]]
   {:db (update db :user-addition-admin-role? not)}))


(re-frame/reg-event-fx
 :add-user
 [interceptors/schema]
 (fn [{:keys [db]} [_ user]]
   {:command {:add-user {:user {:user/id (random-uuid)
                                :user/email-address (:user-addition-email-address db)
                                :user/name (:user-addition-name db)
                                :user/roles (cond-> #{:customer}
                                              (:user-addition-admin-role? db) (conj :admin))}}}
    :db (dissoc db
                :user-addition-email-address
                :user-addition-name
                :user-addition-admin-role?)}))
