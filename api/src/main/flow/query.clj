(ns flow.query
  (:require [flow.entity.user :as user]
            [flow.entity.authorisation :as authorisation]
            [flow.domain.user-management :as user-management]
            [flow.utils :as u]))


(defmulti handle (fn [method payload metadata session] method))


(defmethod handle :current-user
  [_ _ _ {:keys [current-user]}]
  {:users (user/index current-user)})


(defmethod handle :users
  [_ _ metadata {:keys [current-user]}]
  (if (or (user-management/admin? current-user)
          (user-management/customer? current-user))
    (let [{:keys [limit offset]} (:users metadata)
          users (user/fetch-all limit offset)]
      {:users (apply user/index users)
       :metadata {:users {:next-offset (-> users last :user/id)}}})
    {}))


(defmethod handle :user
  [_ {:keys [user/id]} _ _]
  {:users (user/index (user/fetch id))})


(defmethod handle :authorisations
  [_ _ metadata {:keys [current-user]}]
  (if (user-management/admin? current-user)
      (let [{:keys [limit offset]} (:authorisations metadata)
            authorisations (authorisation/fetch-all limit offset)]
        {:authorisations (apply authorisation/index authorisations)
         :metadata {:authorisations {:next-offset (-> authorisations last :authorisation/id)}}})
    {}))


(defmethod handle :default
  [method _ _ _]
  (u/generate :internal-error (str "The query method" method " does not exist.")))
