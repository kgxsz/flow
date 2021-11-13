(ns flow.query
  (:require [flow.entity.user :as user]
            [flow.entity.authorisation :as authorisation]
            [flow.utils :as u]))


(defmulti handle (fn [method payload metadata session] method))


(defmethod handle :current-user
  [_ _ _ {:keys [current-user]}]
  {:users (user/index current-user)})


(defmethod handle :users
  [_ _ {:keys [users]} _]
  {:users (apply user/index (user/fetch-all (:options users)))})


(defmethod handle :user
  [_ {:keys [user/id]} _ _]
  {:users (user/index (user/fetch id))})


(defmethod handle :authorisations
  [_ _ {:keys [authorisations]} _]
  {:authorisations (apply authorisation/index (authorisation/fetch-all (:options authorisations)))})


(defmethod handle :default
  [method _ _ _]
  (u/generate :internal-error (str "The query method" method " does not exist.")))
