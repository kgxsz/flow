(ns flow.domain.utils
  (:require [medley.core :as medley]))


(defn convey-keys
  [{:keys [user/id] :as entity}
   {:keys [user/roles] :as current-user}
   conveyable-keys]
  (let [filter-roles (comp flatten vals (partial medley/filter-keys (partial contains? roles)))
        roles? (some? roles)
        owner? (= id (:id current-user))
        entity (select-keys
                entity
                (cond-> (:public conveyable-keys)
                  roles? (concat (filter-roles (:roles conveyable-keys)))
                  owner? (concat (:owner conveyable-keys))))]
    (when-not (empty? entity) entity)))
