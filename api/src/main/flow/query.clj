(ns flow.query
  (:require [flow.domain.user :as user]
            [flow.domain.authorisation :as authorisation]
            [taoensso.faraday :as faraday]
            [medley.core :as medley]))


(defmulti handle first)


(defmethod handle :current-user
  [[_ {:keys [current-user-id]}]]
  ;; TODO - generalise queries that require a current user to exist
  (if-let [{:keys [user/id] :as current-user} (user/fetch current-user-id)]
    {:current-user
     {id (user/convey-keys current-user current-user)}}
    {:current-user
     {}}))


(defmethod handle :users
  [[_ {:keys [current-user-id]}]]
  (if-let [current-user (user/fetch current-user-id)]
    {:users
     (->> (user/fetch-all)
          (map (partial user/convey-keys current-user))
          (medley/index-by :user/id))}
    {:users
     {}}))


(defmethod handle :authorisation
  [[_ {:keys [authorisation-id current-user-id]}]]
  ;; TODO - this needs to move into a proper spec
  (when-not (uuid? authorisation-id)
    (throw (IllegalArgumentException. "Unsupported query parameters.")))
  (let [current-user (user/fetch current-user-id)
        {:keys [authorisation/id] :as authorisation} (authorisation/fetch authorisation-id)]
    {:authorisation
     {id (authorisation/convey-keys current-user authorisation)}}))


(defmethod handle :authorisations
  [[_ {:keys [current-user-id]}]]
  (if-let [current-user (user/fetch current-user-id)]
    {:authorisations
     (->> (authorisation/fetch-all)
          (map (partial authorisation/convey-keys current-user))
          (medley/index-by :authorisation/id))}
    {:authorisations
     {}}))


(defmethod handle :default
  [query]
  (throw (IllegalArgumentException. "Unsupported query method.")))
