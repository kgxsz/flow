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
