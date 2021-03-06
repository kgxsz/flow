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
     :current-user {:db (update db :user merge (:current-user response))}
     :users {:db (update db :user merge (:users response))}
     :user {:db (update db :user merge (:user response))}
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
     :initialise-authorisation {:db db}
     :finalise-authorisation {:db (assoc db
                                         :authorisation-finalised? false
                                         :authorisation-failed? false)
                              ;; TODO - this needs to lead to the auth
                              ;; finalise becoming true when the user
                              ;; arrives? Or is the loading page enough?
                              :query {:current-user {}}}
     :deauthorise {:db db}
     :add-user (if (:user-id response)
                 {:query {:user {:user-id (:user-id response)}}
                  :db db}
                 {:db db})
     :delete-user (if (:user-id response)
                    {:query {:user {:user-id (:user-id response)}}
                     :db db}
                    {:db db})
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
 :initialise-authorisation
 [interceptors/schema]
 (fn [{:keys [db]} [_]]
   (let [{:keys [authorisation-initialised?]} db]
     (if authorisation-initialised?
       {:db db}
       {:command {:initialise-authorisation (select-keys
                                             db
                                             [:authorisation-email-address])}
        :db (assoc db :authorisation-initialised? true)}))))


(re-frame/reg-event-fx
 :finalise-authorisation
 [interceptors/schema]
 (fn [{:keys [db]} [_]]
   (let [{:keys [authorisation-finalised?]} db]
     (if authorisation-finalised?
       {:db db}
       {:command {:finalise-authorisation (select-keys
                                           db
                                           [:authorisation-phrase
                                            :authorisation-email-address])}
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
   {:command {:delete-user {:user-id user-id}}
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
   {:command {:add-user {:user {:email-address (:user-addition-email-address db)
                                :name (:user-addition-name db)
                                :roles (cond-> #{:customer}
                                         (:user-addition-admin-role? db) (conj :admin))}}}
    :db (dissoc db
                :user-addition-email-address
                :user-addition-name
                :user-addition-admin-role?)}))
