(ns flow.db
  (:require [taoensso.faraday :as faraday]))


(def config {:access-key (System/getenv "ACCESS_KEY")
             :secret-key (System/getenv "SECRET_KEY")
             :endpoint (System/getenv "DB_ENDPOINT")
             :batch-write-limit 25})


(defn entity-partition
  [entity-type id]
  (str (name entity-type) ":" id))


(defn put-entity [entity-type entity-id entity]
  (faraday/put-item
   config
   :flow
   {:partition (entity-partition entity-type entity-id)
    :entity (faraday/freeze entity)})
  entity-id)


(defn fetch-entity [entity-type entity-id]
  (:entity
   (faraday/get-item
    config
    :flow
    {:partition (entity-partition entity-type entity-id)})))


(defn update-entity [entity-type entity-id f]
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
    (throw (IllegalArgumentException.
            (str "the " entity-type " entity with id " entity-id " does not exist.")))))


(defn fetch-entities [entity-type]
  (let [result (faraday/scan
                config
                :flow
                {:attr-conds {:partition [:begins-with (name entity-type)]}})]
    (mapv :entity result)))
