(ns flow.query
  (:require [flow.domain.user :as user]
            [flow.domain.authorisation :as authorisation]
            [taoensso.faraday :as faraday]
            [medley.core :as medley]))


(defmulti handle first)


(defmethod handle :current-user
  [[_ {:keys [current-user-id]}]]
  ;; TODO - Eventually generalise dealing with queries that require a current user
  (if-let [{:keys [user/id] :as current-user} (user/fetch current-user-id)]
    {:current-user
     {id (user/convey-keys current-user current-user)}}
    {:current-user
     {}}))


#_(defmethod handle :user
  [[_ {:keys [user-id current-user-id]}]]
  (when-not (uuid? user-id)
    (throw (IllegalArgumentException. "Unsupported query parameters.")))
  (let [{:keys [user/id] :as user} (user/fetch user-id)
        current-user (user/fetch current-user-id)]
    {:user
     {id (user/convey-keys current-user user)}}))


(defmethod handle :users
  [[_ {:keys [current-user-id]}]]
  (if-let [{:keys [user/id] :as current-user} (user/fetch current-user-id)]
    {:users
     (->> (user/fetch-all)
          (map (partial user/convey-keys current-user))
          (medley/index-by :user/id))}
    {:users
     {}}))


(defmethod handle :authorisation
  [[_ {:keys [authorisation-id current-user-id]}]]
  (when-not (uuid? authorisation-id)
    (throw (IllegalArgumentException. "Unsupported query parameters.")))
  (let [current-user (user/fetch current-user-id)
        {:keys [authorisation/id] :as authorisation} (authorisation/fetch authorisation-id)]
    {:authorisation
     {id (authorisation/convey-keys current-user authorisation)}}))


(defmethod handle :default
  [query]
  (throw (IllegalArgumentException. "Unsupported query method.")))
