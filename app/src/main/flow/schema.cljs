(ns flow.schema
  (:require [cljs.spec.alpha :as s]
            [clojure.string :as string]
            [medley.core :as medley]))


(s/def ::db (s/keys :req-un []
                    :opt-un [::route
                             ::route-params
                             ::query-params]))
