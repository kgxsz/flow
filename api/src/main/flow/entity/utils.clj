(ns flow.entity.utils
  (:require [medley.core :as medley]
            [expound.alpha :as expound]))


(defn index-entities
  "Returns a map with keys equal to the ids of the provided entities,
   and values equal to the entities themselves. If any keys are nil,
   both the key and value will be removed from the resulting map."
  [key entities]
  (->> entities
       (medley/index-by key)
       (medley/remove-keys nil?)))


(defn select-default-accessible-keys
  "Returns the provided entity with only the default accessible keys present.
   If there are no default accessible keys, then returns an empty map."
  [default-accessible-keys entity]
  (select-keys entity default-accessible-keys))


(defn select-owner-accessible-keys
  "Returns the provided entity with only the owner accessible keys present,
   If the current user does not own the entity, then returns an empty map."
  [owner-accessible-keys current-user entity]
  (if (= (:user/id current-user) (:user/id entity))
    (select-keys entity owner-accessible-keys)
    {}))


(defn select-role-accessible-keys
  "Returns the provided entity with only the role accessible
   keys present, given the current user's roles. If there are
   no matching roles or extracted keys, then returns an empty map."
  [role-accessible-keys current-user entity]
  (or (->> (:user/roles current-user)
           (map (comp (partial select-keys entity) (partial role-accessible-keys)))
           (apply merge))
      {}))
