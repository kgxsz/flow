(ns flow.db
  (:require [taoensso.faraday :as faraday]
            [clojure.spec.alpha :as s]
            [expound.alpha :as expound]))


(def config {:access-key (System/getenv "ACCESS_KEY")
             :secret-key (System/getenv "SECRET_KEY")
             :endpoint (System/getenv "DB_ENDPOINT")
             :batch-write-limit 25})


(defn entity-specification
  "Generates the spec key for the given entity."
  [entity-type]
  (keyword "db" (name entity-type)))


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

  ;; TODO - utilitify these specs
  (when-not (s/valid? :db/entity-partition (entity-partition entity-type entity-id))
    (expound/expound :db/entity-partition (entity-partition entity-type entity-id))
    (throw (IllegalStateException.
            (str "the partition violates specification."))))

  (when-not (s/valid? (entity-specification entity-type) entity)
    (expound/expound (entity-specification entity-type) entity)
    (throw (IllegalStateException.
            (str "the " entity-type " entity with id " entity-id " violates specification."))))

  (if (nil? (fetch-entity entity-type entity-id))
    (do
      (faraday/put-item
       config
       :flow
       {:partition (entity-partition entity-type entity-id)
        :entity (faraday/freeze entity)})
      entity-id)
    (throw
     (IllegalStateException.
      (str "the " entity-type " entity with id " entity-id " already exists.")))))


(defn mutate-entity!
  "Mutates an entity by applying function f if and only if the
   entity exists. On success, returns the entity's id."
  [entity-type entity-id f]
  (if-let [entity (fetch-entity entity-type entity-id)]
    (let [entity (f entity)]

      (when-not (s/valid? :db/entity-partition (entity-partition entity-type entity-id))
        (expound/expound :db/entity-partition (entity-partition entity-type entity-id))
        (throw (IllegalStateException.
                (str "the partition violates specification."))))

      (when-not (s/valid? (entity-specification entity-type) entity)
        (expound/expound (entity-specification entity-type) entity)
        (throw (IllegalStateException.
                (str "the " entity-type " entity with id " entity-id " violates specification."))))

      (faraday/update-item
       config
       :flow
       {:partition (entity-partition entity-type entity-id)}
       {:update-expr "SET entity = :entity"
        :expr-attr-vals {":entity" (faraday/freeze entity)}
        :return :all-new})
      entity-id)
    (throw
     (IllegalStateException.
      (str "the " entity-type " entity with id " entity-id " does not exist.")))))
