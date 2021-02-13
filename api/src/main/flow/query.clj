(ns flow.query
  (:require [flow.domain.user :as user]
            [flow.domain.authorisation :as authorisation]
            [taoensso.faraday :as faraday]))


(defmulti handle first)


(defmethod handle :user
  [[_ {:keys [user-id current-user-id]}]]
  (when-not (uuid? user-id)
    (throw (IllegalArgumentException. "Unsupported query parameters.")))
  (let [user (user/fetch user-id)
        current-user (user/fetch current-user-id)]
    {:user
     {user-id (user/convey-keys user current-user)}}))


(defmethod handle :authorisation
  [[_ {:keys [authorisation-id current-user-id]}]]
  (when-not (uuid? authorisation-id)
    (throw (IllegalArgumentException. "Unsupported query parameters.")))
  (let [authorisation (authorisation/fetch authorisation-id)
        current-user (user/fetch current-user-id)]
    {:authorisation
     {authorisation-id (authorisation/convey-keys authorisation current-user)}}))


(defmethod handle :default
  [query]
  (throw (IllegalArgumentException. "Unsupported query method.")))
