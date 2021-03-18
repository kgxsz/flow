(ns flow.db
  (:require [taoensso.faraday :as faraday]))


(def config {:access-key (System/getenv "ACCESS_KEY")
             :secret-key (System/getenv "SECRET_KEY")
             :endpoint (System/getenv "DB_ENDPOINT")
             :batch-write-limit 25})


(defn entity-partition
  "Generates the partitition key for DynamoDB,
   made up of the given entity type and its id."
  [entity-type id]
  (str (name entity-type) ":" id))


(defn fetch-entity
  "Fetches an entity from DynamoDB if it exists.
   If it doesn't exists, returns nil."
  [entity-type entity-id]
  (:entity
   (faraday/get-item
    config
    :flow
    {:partition (entity-partition entity-type entity-id)})))


(defn fetch-entities
  "Fetches all entities of the given entity type."
  [entity-type]
  (let [result (faraday/scan
                config
                :flow
                {:attr-conds {:partition [:begins-with (name entity-type)]}})]
    (mapv :entity result)))


(defn put-entity!
  "Puts an entity into DynamoDB if and only if the entity doesn't
   already exist. On success, returns the entity's id."
  [entity-type entity-id entity]
  (if (nil? (fetch-entity entity-type entity-id))
    (do
      (faraday/put-item
       config
       :flow
       {:partition (entity-partition entity-type entity-id)
        :entity (faraday/freeze entity)})
      entity-id)
    (throw
     (IllegalArgumentException.
      (str "the " entity-type " entity with id " entity-id " already exists.")))))


(defn mutate-entity!
  "Mutates an entity by applying function f if and only if the
   entity exists. On success, returns the entity's id."
  [entity-type entity-id f]
  (if-let [entity (fetch-entity entity-type entity-id)]
    (do
      (faraday/update-item
       config
       :flow
       {:partition (entity-partition entity-type entity-id)}
       {:update-expr "SET entity = :entity"
        :expr-attr-vals {":entity" (faraday/freeze (f entity))}
        :return :all-new})
      entity-id)
    (throw
     (IllegalArgumentException.
      (str "the " entity-type " entity with id " entity-id " does not exist.")))))
