(ns flow.query
  (:require [flow.entity.user :as user]
            [flow.entity.authorisation :as authorisation]
            [flow.utils :as u]
            [taoensso.faraday :as faraday]))


(defmulti handle (fn [method payload metadata] method))


(defmethod handle :current-user
  [_ _ {:keys [current-user]}]
  {:users (->> current-user
               (user/filter-sanctioned-keys current-user)
               (u/index :user/id))})


(defmethod handle :users
  [_ _ {:keys [current-user]}]
  {:users (->> (user/fetch-all)
               (map (partial user/filter-sanctioned-keys current-user))
               (map (partial u/index :user/id))
               (into {}))})


(defmethod handle :user
  [_ {:keys [user/id]} {:keys [current-user]}]
  {:users (->> (user/fetch id)
               (user/filter-sanctioned-keys current-user)
               (u/index :user/id))})


(defmethod handle :authorisations
  [_ _ {:keys [current-user]}]
  {:authorisations (->> (authorisation/fetch-all)
                        (map (partial authorisation/filter-sanctioned-keys current-user))
                        (map (partial u/index :authorisation/id))
                        (into {}))})


(defmethod handle :default
  [_ _ _]
  (throw (IllegalArgumentException. "Unsupported query method.")))
