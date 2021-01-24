(ns flow.events
  (:require [re-frame.core :as re-frame]
            [flow.interceptors :as interceptors]
            [goog.string :as gstring]
            [goog.string.format]))


(re-frame/reg-event-fx
 :initialise
 [interceptors/schema]
 (fn [{:keys [db]} event]
   {:db {}
    :initialise-routing {}
    :query {:user {}}}))


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
       :unknown {:db db}
       {:db db}))))


(re-frame/reg-event-fx
 :update-route
 [interceptors/schema]
 (fn [{:keys [db]} [_ route]]
   {:update-route {:route route}}))


(re-frame/reg-event-fx
 :query-success
 [interceptors/schema interceptors/current-user-id]
 (fn [{:keys [db]} [_ query response]]
   (case (-> query keys first)
     :user {:db (update db :user merge (:user response))}
     {})))


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
     :initialise-authorisation {}
     :finalise-authorisation {:query {:user {}}}
     :deauthorise {}
     {})))


(re-frame/reg-event-fx
 :command-failure
 [interceptors/schema interceptors/current-user-id]
 (fn [{:keys [db]} [_ command response]]
   {:db (assoc db :error? true)}))


(re-frame/reg-event-fx
 :initialise-authorisation
 [interceptors/schema]
 (fn [{:keys [db]} [_]]
   {:command {:initialise-authorisation {}}
    :db db}))


(re-frame/reg-event-fx
 :finalise-authorisation
 [interceptors/schema]
 (fn [{:keys [db]} [_ parameters]]
   {:command {:finalise-authorisation parameters}
    :db db}))


(re-frame/reg-event-fx
 :deauthorise
 [interceptors/schema]
 (fn [{:keys [db]} [_]]
   {:command {:deauthorise {}}
    :db (-> db
            (dissoc :current-user-id)
            (update :user dissoc (:current-user-id db)))}))
