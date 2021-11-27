(ns flow.query
  (:require [flow.entity.user :as user]
            [flow.entity.authorisation :as authorisation]
            [flow.utils :as u]))


(defmulti handle (fn [method payload metadata session] method))


(defmethod handle :current-user
  [_ _ _ {:keys [current-user]}]
  {:users (user/index current-user)})


(defmethod handle :users
  [_ _ metadata {:keys [current-user]}]
  (let [offset (get-in metadata [:users :offset])
        limit (get-in metadata [:users :limit] 10)
        users (user/fetch-all limit offset)
        exhausted? (< (count users) limit)]
    {:users (apply user/index users)
     :metadata {:users {:next-offset (when-not exhausted? (-> users last :user/id))}}}))


(defmethod handle :user
  [_ {:keys [user/id]} _ _]
  {:users (user/index (user/fetch id))})


(defmethod handle :authorisations
  [_ _ metadata {:keys [current-user]}]
  (let [offset (get-in metadata [:authorisations :offset])
        limit (get-in metadata [:authorisations :limit] 10)
        authorisations (authorisation/fetch-all limit offset)
        exhausted? (< (count authorisations) limit)]
    {:authorisations (apply authorisation/index authorisations)
     :metadata {:authorisations {:next-offset (when-not exhausted? (-> authorisations last :authorisation/id))}}}))


(defmethod handle :default
  [method _ _ _]
  (u/generate :internal-error (str "The query method" method " does not exist.")))
