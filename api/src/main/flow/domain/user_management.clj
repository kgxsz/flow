(ns flow.domain.user-management
  (:require [flow.entity.user :as user]
            [clj-time.coerce :as t.coerce]
            [clj-time.core :as t]))


(defn delete
  "Marks the user as deleted by specifying the
   time of deletion as now, if and only if
   the user has not previously been deleted."
  [{:keys [user/deleted-at] :as user}]
  (cond-> user
    (nil? deleted-at)
    (assoc :user/deleted-at (t.coerce/to-date (t/now)))))


(defn admin?
  "Given a user, determines if it is an admin."
  [{:keys [user/roles]}]
  (contains? roles :admin))
