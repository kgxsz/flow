(ns flow.domain.utils
  (:require [medley.core :as medley]))


(defn filter-sanctioned-keys
  "Given the roles of the current user, and whether or not the current user is the
   owner of the entity, filters the entity's keys such that only relevent sanctioned
   keys are left. If no keys are found to be sanctioned, then returns nil."
  [default-sanctioned-keys
   owner-sanctioned-keys
   role-sanctioned-keys
   {:keys [user/roles] :as current-user}
   {:keys [user/id] :as entity}]
  (let [filter-sanctioned-keys-by-role (comp (partial apply clojure.set/union)
                                          vals
                                          (partial medley/filter-keys (partial contains? roles)))
        owner? (= id (:user/id current-user))
        roles? (not (empty? roles))
        sanctioned-keys (cond-> default-sanctioned-keys
                          owner? (clojure.set/union owner-sanctioned-keys)
                          roles? (clojure.set/union (filter-sanctioned-keys-by-role role-sanctioned-keys)))
        entity (medley/filter-keys (partial contains? sanctioned-keys) entity)]
    (when-not (empty? entity) entity)))
