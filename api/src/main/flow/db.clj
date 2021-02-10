(ns flow.db
  (:require [taoensso.faraday :as faraday]))


(def config {:access-key (System/getenv "ACCESS_KEY")
             :secret-key (System/getenv "SECRET_KEY")
             :endpoint (System/getenv "DB_ENDPOINT")
             :batch-write-limit 25})

(defn generate-partition
  [entity-type id]
  (str (name entity-type) ":" id))


(defn put-entity [entity-type {:keys [id] :as entity}]
  (faraday/put-item
   config
   :flow
   {:partition (generate-partition entity-type id)
    :entity (faraday/freeze entity)}))


(defn fetch-entity [entity-type id]
  (:entity
   (faraday/get-item
    config
    :flow
    {:partition (generate-partition entity-type id)})))


(defn update-entity [entity-type id f]
  (if-let [entity (fetch-entity entity-type id)]
    (faraday/update-item
     config
     :flow
     {:partition (generate-partition entity-type id)}
     {:update-expr "SET entity = :entity"
      :expr-attr-vals {":entity" (faraday/freeze (f entity))}
      :return :all-new})
    (throw (IllegalArgumentException.
            (str "the entity " (generate-partition entity-type id) " does not exist.")))))


(defn fetch-entities [entity-type]
  (faraday/scan
   config
   :flow
   {:attr-conds {:partition [:begins-with (name entity-type)]}}))
