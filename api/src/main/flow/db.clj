(ns flow.db
  (:require [taoensso.faraday :as faraday]
            [flow.utils :as u]
            [slingshot.slingshot :as slingshot]))


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
   (slingshot/try+
    (faraday/get-item
     config
     :flow
     {:partition (entity-partition entity-type entity-id)})
    (catch Object _
      (u/generate :external-error "Unable to get an item from DynamoDB.")))))


(defn fetch-entities
  "Fetches all entities of the given entity type, sorted by the
   entity's id, with a maximum number of items given by the limit,
   offset by the item identified with the offset-entity-id."
  [entity-type {:keys [limit offset-entity-id]}]
  (let [entity-id-key (keyword (name entity-type) "id")
        result (slingshot/try+
                (faraday/scan
                 config
                 :flow
                 {:attr-conds {:partition [:begins-with (name entity-type)]}})
                (catch Object _
                  (u/generate :external-error "Unable to scan DynamoDB.")))]
    (->> result
         (map :entity)
         (sort-by entity-id-key)
         (partition-by #(= offset-entity-id (get % entity-id-key)))
         (last)
         (remove #(= offset-entity-id (get % entity-id-key)))
         (take limit)
         (into []))))


(defn create-entity!
  "Creates an entity into DynamoDB if and only if the entity doesn't
   already exist. On success, returns the entity's id."
  [entity-type entity-id entity]
  (if (nil? (fetch-entity entity-type entity-id))
    (slingshot/try+
      (faraday/put-item
       config
       :flow
       {:partition (u/validate :db/entity-partition (entity-partition entity-type entity-id))
        :entity (->> entity
                     (u/validate (entity-specification entity-type))
                     (faraday/freeze))})
      entity-id
      (catch [:type :flow/internal-error] _ (slingshot/throw+))
      (catch Object _
        (u/generate :external-error "Unable to put an item into DynamoDB.")))
    (slingshot/throw+
     {:type :flow/internal-error
      :message (str "the " entity-type " entity with id " entity-id " already exists.")})))


(defn mutate-entity!
  "Mutates an entity by applying function f if and only if the
   entity exists. On success, returns the entity's id."
  [entity-type entity-id f]
  (if-let [entity (fetch-entity entity-type entity-id)]
    (slingshot/try+
      (faraday/update-item
       config
       :flow
       {:partition (u/validate :db/entity-partition (entity-partition entity-type entity-id))}
       {:update-expr "SET entity = :entity"
        :expr-attr-vals {":entity" (->> entity
                                        (f)
                                        (u/validate (entity-specification entity-type))
                                        (faraday/freeze))}
        :return :all-new})
      entity-id
      (catch [:type :flow/internal-error] _ (slingshot/throw+))
      (catch Object _
        (u/generate :external-error "Unable to update an item in DynamoDB.")))
    (slingshot/throw+
     {:type :flow/internal-error
      :message (str "the " entity-type " entity with id " entity-id " does not exist.")})))
