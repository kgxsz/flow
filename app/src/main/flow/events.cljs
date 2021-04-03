(ns flow.events
  (:require [re-frame.core :as re-frame]
            [flow.interceptors :as interceptors]
            [clojure.string :as string]))


(re-frame/reg-event-fx
 :initialise
 [interceptors/schema]
 (fn [{:keys [db]} event]
   {:db {}
    ;; TODO - consider renaming
    :initialise-routing {}
    :api {:query {:current-user {}}}}))


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
                     :api {:query {:users {}}}}
       :admin.authorisations {:db db
                              :api {:query {:authorisations {}}}}
       :unknown {:db db}
       {:db db}))))


(re-frame/reg-event-fx
 :update-route
 [interceptors/schema]
 (fn [{:keys [db]} [_ route]]
   {:update-route {:route route
                   :db db}}))


(re-frame/reg-event-fx
 ;; TODO - consider naming
 :handle-api-success
 [interceptors/schema interceptors/current-user-id]
 (fn [{:keys [db]} [_ parameters {:keys [users authorisations metadata]}]]
   {:db (cond-> db
          users (update :user merge users)
          authorisations (update :authorisation merge authorisations)
          ;; TODO - do something smarter here
          (get-in parameters [:command :finalise-authorisation-attempt])
          (assoc :authorisation-finalised? false
                 :authorisation-failed? false))}))


(re-frame/reg-event-fx
 :handle-api-failure
 [interceptors/schema interceptors/current-user-id]
 (fn [{:keys [db]} [_ parameters response]]
   {:db (assoc db :error? true)}))


(re-frame/reg-event-fx
 :update-authorisation-email-address
 [interceptors/schema]
 (fn [{:keys [db]} [_ input-value]]
   (let [valid-length? (<= (count input-value) 250)
         sanitise #(string/replace % #"\n|\r| " "")]
     (if valid-length?
       {:db (assoc db :authorisation-email-address (sanitise input-value))}
       {:db db}))))


(re-frame/reg-event-fx
 :update-authorisation-phrase
 [interceptors/schema]
 (fn [{:keys [db]} [_ input-value]]
   (let [valid-length? (<= (count input-value) 250)
         sanitise #(string/replace % #"\n|\r| " "")]
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
       {:api {:command {:initialise-authorisation-attempt
                        {:user/email-address (:authorisation-email-address db)}}}
        :db (assoc db :authorisation-initialised? true)}))))


(re-frame/reg-event-fx
 ;; TODO - attempt
 :finalise-authorisation
 [interceptors/schema]
 (fn [{:keys [db]} [_]]
   (let [{:keys [authorisation-finalised?]} db]
     (if authorisation-finalised?
       {:db db}
       {:api {:command {:finalise-authorisation-attempt
                        {:user/email-address (:authorisation-email-address db)
                         :authorisation/phrase (:authorisation-phrase db)}}
              :query {:current-user {}}}
        :db (assoc db :authorisation-finalised? true)}))))


(re-frame/reg-event-fx
 :deauthorise
 [interceptors/schema]
 (fn [{:keys [db]} [_]]
   {:api {:command {:deauthorise {}}}
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
   {:api {:command {:delete-user {:user/id user-id}}
          :query {:user {:user/id user-id}}}
    :db db}))


(re-frame/reg-event-fx
 :update-user-addition-name
 [interceptors/schema]
 (fn [{:keys [db]} [_ input-value]]
   (let [valid-length? (<= (count input-value) 250)
         sanitise #(string/replace % #"\n|\r| " "")]
     (if valid-length?
       {:db (assoc db :user-addition-name (sanitise input-value))}
       {:db db}))))


(re-frame/reg-event-fx
 :update-user-addition-email-address
 [interceptors/schema]
 (fn [{:keys [db]} [_ input-value]]
   (let [valid-length? (<= (count input-value) 250)
         sanitise #(string/replace % #"\n|\r| " "")]
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
   {:api (let [id (random-uuid)]
           {:command {:add-user {:user/id id
                                 :user/email-address (:user-addition-email-address db)
                                 :user/name (:user-addition-name db)
                                 :user/roles (cond-> #{:customer}
                                               (:user-addition-admin-role? db) (conj :admin))}}
            :query {:user {:user/id id}}})
    :db (dissoc db
                :user-addition-email-address
                :user-addition-name
                :user-addition-admin-role?)}))
