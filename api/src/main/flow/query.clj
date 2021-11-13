(ns flow.query
  (:require [flow.entity.user :as user]
            [flow.entity.authorisation :as authorisation]
            [flow.utils :as u]))


(defmulti handle (fn [method payload metadata session] method))


(defmethod handle :current-user
  [_ _ _ {:keys [current-user]}]
  {:users (user/index current-user)})


(defmethod handle :users
  [_ _ metadata _]
  (let [{:keys [limit offset]} (get-in metadata [:users :options])
        options {:limit (or limit 10) :offset offset}]
    {:users (apply user/index (user/fetch-all options))}))


(defmethod handle :user
  [_ {:keys [user/id]} _ _]
  {:users (user/index (user/fetch id))})


(defmethod handle :authorisations
  [_ _ metadata _]
  (let [{:keys [limit offset]} (get-in metadata [:authorisations :options])
        options {:limit (or limit 10) :offset offset}]
    {:authorisations (apply authorisation/index (authorisation/fetch-all options))}))


(defmethod handle :default
  [method _ _ _]
  (u/generate :internal-error (str "The query method" method " does not exist.")))
