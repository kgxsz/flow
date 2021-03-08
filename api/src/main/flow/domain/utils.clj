(ns flow.domain.utils
  (:require [medley.core :as medley]))


(defn filter-sanctioned-keys
  "Given the roles of the current user, and whether or not the current user is the
   owner of the entity, filters the entity's keys such that only relevent sanctioned
   keys are left. If no keys are found to be sanctioned, then returns nil."
  [sanctioned-keys
   {:keys [user/roles] :as current-user}
   {:keys [user/id] :as entity}]
  (let [owner? (= id (:user/id current-user))
        filter-roles (comp (partial apply clojure.set/union) vals (partial medley/filter-keys (partial contains? roles)))
        sanctioned-keys (clojure.set/union
                         (:default sanctioned-keys)
                         (when owner? (:owner sanctioned-keys))
                         (filter-roles (:role sanctioned-keys)))
        entity (medley/filter-keys (partial contains? sanctioned-keys) entity)]
    (when-not (empty? entity) entity)))
