(ns flow.domain.admin
  (:require [flow.entity.user :as user]
            [clj-time.coerce :as t.coerce]
            [clj-time.core :as t]))


(defn delete
  "Marks the user at the given id as deleted.
   Doesn't actually remove the entity."
  [id]
  (user/update
   id
   #(cond-> %
      (nil? (:user/deleted-at %))
      (assoc :user/deleted-at (t.coerce/to-date (t/now))))))


(defn priveledged?
  "Given a user, determines if it has an admin priveledges."
  [{:keys [user/roles]}]
  (contains? roles :admin))
