(ns flow.query
  (:require [flow.entity.user :as user]
            [flow.entity.authorisation :as authorisation]))


(defmulti handle (fn [method payload metadata] method))


(defmethod handle :current-user
  [_ _ {:keys [current-user]}]
  {:users (user/index current-user)})


(defmethod handle :users
  [_ _ _]
  {:users (apply user/index (user/fetch-all))})


(defmethod handle :user
  [_ {:keys [user/id]} _]
  {:users (user/index (user/fetch id))})


(defmethod handle :authorisations
  [_ _ _]
  {:authorisations (apply authorisation/index (authorisation/fetch-all))})


(defmethod handle :default
  [_ _ _]
  (throw (IllegalArgumentException. "Unsupported query method.")))
