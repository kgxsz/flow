(ns flow.domain.user-management
  (:require [flow.entity.user :as user]
            [clj-time.coerce :as t.coerce]
            [clj-time.core :as t]))


(defn delete!
  "Marks the user at the given id as deleted.
   Doesn't actually remove the entity."
  [id]
  (user/mutate!
   id
   #(cond-> %
      (nil? (:user/deleted-at %))
      (assoc :user/deleted-at (t.coerce/to-date (t/now))))))


(defn admin?
  "Given a user, determines if it is an admin."
  [{:keys [user/roles]}]
  (contains? roles :admin))


(defn exists?
  "Given an id, determines if a user
   with that email address already exists."
  [id]
  (some? (user/fetch id)))
