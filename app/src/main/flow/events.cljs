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
    :command {:initialise {}}}))


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
   {:update-route {:route route
                   :db db}}))


(re-frame/reg-event-fx
 :query-success
 [interceptors/schema interceptors/current-user-id]
 (fn [{:keys [db]} [_ query response]]
   (case (-> query keys first)
     :user {:db (update db :user merge (:user response))}
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
     :finalise-authorisation {:db (if (:current-user-id db)
                                    (assoc db
                                           :authorisation-finalised? false
                                           :authorisation-failed? false)
                                    (assoc db
                                           :authorisation-finalised? false
                                           :authorisation-failed? true))}
     :deauthorise {:db db}
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
