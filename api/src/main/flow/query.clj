(ns flow.query
  (:require [flow.domain.user :as user]
            [taoensso.faraday :as faraday]))


(defmulti handle first)


(defmethod handle :user
  [[_ {:keys [user-id current-user-id]}]]
  (when-not (uuid? user-id)
    (throw (IllegalArgumentException. "Unsupported query parameters.")))
  (let [user (user/fetch user-id)
        current-user (user/fetch current-user-id)]
    {:user {user-id (user/strip user current-user)}}))


(defmethod handle :default
  [query]
  (throw (IllegalArgumentException. "Unsupported query method.")))
