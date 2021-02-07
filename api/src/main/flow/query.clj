(ns flow.query
  (:require [flow.db :as db]
            [taoensso.faraday :as faraday]))


(defn query-user [user-id]
  (let [query {:partition (str "user:" user-id)}]
    (:data (faraday/get-item db/config :flow query))))


(defmulti handle first)


(defmethod handle :user [[_ {:keys [user-id current-user-id]}]]
  (if-let [user-id (or user-id current-user-id)]
    {:user {user-id (query-user user-id)}}
    {:user {}}))


(defmethod handle :default [query]
  (throw (IllegalArgumentException. "Unsupported query method.")))
