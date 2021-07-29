(ns flow.interceptors
  (:require [cljs.spec.alpha :as s]
            [expound.alpha :as expound]
            [re-frame.core :as re-frame]
            [flow.schema :as schema]))


(def schema
  (re-frame/after
   (fn [db]
     (when-not (s/valid? ::schema/db db)
       (js/console.error (expound/expound-str ::schema/db db))
       (throw (ex-info "the db spec has been violated"
                       {:spec ::schema/db
                        :db db}))))))


(def log
  (re-frame/after
   (fn [db]
     (js/console.info db))))


(def current-user-id
  (re-frame/->interceptor
   :id :current-user-id
   :before (fn [context]
             (let [{:keys [current-user-id]} (get-in context [:coeffects :event 2 :session])]
               (-> context
                   (update-in [:coeffects :event 2 :session] dissoc :current-user-id)
                   (assoc-in [:coeffects :db :current-user-id] current-user-id))))
   :after (fn [context]
            (if (some? (get-in context [:effects :db]))
              context
              (assoc-in context [:effects :db] (get-in context [:coeffects :db]))))))
