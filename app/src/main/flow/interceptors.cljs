(ns flow.interceptors
  (:require [cljs.spec.alpha :as s]
            [expound.alpha :as expound]
            [re-frame.core :as re-frame]
            [flow.specifications :as specifications]))


(def validate-db
  (re-frame/after
   (fn [db]
     (when-not (s/valid? :app/db db)
       (js/console.error (expound/expound-str :app/db db))
       (throw (ex-info "the db specification has been violated"
                       {:spec :app/db
                        :db db}))))))


(def log
  (re-frame/after
   (fn [db]
     (js/console.info db))))
