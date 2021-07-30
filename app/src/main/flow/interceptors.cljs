(ns flow.interceptors
  (:require [cljs.spec.alpha :as s]
            [expound.alpha :as expound]
            [re-frame.core :as re-frame]
            [flow.specifications :as specifications]))


(def schema
  (re-frame/after
   (fn [db]
     (when-not (s/valid? :app/db db)
       (js/console.error (expound/expound-str :app/db db))
       (throw (ex-info "the db spec has been violated"
                       {:spec :app/db
                        :db db}))))))


(def log
  (re-frame/after
   (fn [db]
     (js/console.info db))))


(def session
  (re-frame/->interceptor
   :id :session
   :before (fn [context]
             (let [session (get-in context [:coeffects :event 2 :session])]
               (-> context
                   (update-in [:coeffects :event 2] dissoc :session)
                   (assoc-in [:coeffects :db :session] session))))
   :after (fn [context]
            (if (some? (get-in context [:effects :db]))
              context
              (assoc-in context [:effects :db] (get-in context [:coeffects :db]))))))
