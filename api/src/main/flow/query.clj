(ns flow.query
  (:require [flow.entity.user :as user]
            [flow.entity.authorisation :as authorisation]
            [taoensso.faraday :as faraday]
            [flow.utils :as u]))


(defmulti handle first)


(defmethod handle :current-user
  [[_ {:keys [current-user-id]}]]
  ;; TODO - current user could have been sourced as part of a session
  (let [current-user (user/fetch current-user-id)]
    {:users (->> current-user
                 (user/filter-sanctioned-keys current-user)
                 (u/index :user/id))}))


(defmethod handle :users
  [[_ {:keys [current-user-id]}]]
  ;; TODO - current user could have been sourced as part of a session
  (let [current-user (user/fetch current-user-id)]
    {:users (->> (user/fetch-all)
                 (map (partial user/filter-sanctioned-keys current-user))
                 (map (partial u/index :user/id))
                 (into {}))}))


(defmethod handle :user
  [[_ {:keys [user/id current-user-id]}]]
  ;; TODO - this needs to move into a proper spec
  (when-not (uuid? id)
    (throw (IllegalArgumentException. "Unsupported query parameters.")))
  ;; TODO - current user could have been sourced as part of a session
  (if-let [current-user (user/fetch current-user-id)]
    {:users (->> (user/fetch id)
                 (user/filter-sanctioned-keys current-user)
                 (u/index :user/id))}))


(defmethod handle :authorisations
  [[_ {:keys [current-user-id]}]]
  ;; TODO - current user could have been sourced as part of a session
  (let [current-user (user/fetch current-user-id)]
    {:authorisations (->> (authorisation/fetch-all)
                          (map (partial authorisation/filter-sanctioned-keys current-user))
                          (map (partial u/index :authorisation/id))
                          (into {}))}))


(defmethod handle :default
  [query]
  (throw (IllegalArgumentException. "Unsupported query method.")))
