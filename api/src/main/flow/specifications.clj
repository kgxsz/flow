(ns flow.specifications
  (:require [clojure.spec.alpha :as s]))


(s/def :user/id uuid?)
(s/def :user/name string?)
(s/def :user/email-address string?)
(s/def :user/created-at inst?)
(s/def :user/deleted-at (s/nilable inst?))

(s/def :authorisation/id uuid?)
(s/def :authorisation/phrase string?)
(s/def :authorisation/created-at inst?)
(s/def :authorisation/granted-at (s/nilable inst?))

(s/def :db/user (s/keys :req [:user/id
                              :user/name
                              :user/email-address
                              :user/roles
                              :user/created-at
                              :user/deleted-at]))
(s/def :db/authorisation (s/keys :req [:authorisation/id
                                       :user/id
                                       :authorisation/phrase
                                       :authorisation/created-at
                                       :authorisation/granted-at]))

(s/def :command/initialise-authorisation-attempt (s/keys :req [:user/email-address]))
(s/def :command/finalise-authorisation-attempt (s/keys :req [:user/email-address
                                                             :authorisation/phrase]))
(s/def :command/deauthorise (s/and map? empty?))
(s/def :command/add-user (s/keys :req [:user/id
                                       :user/name
                                       :user/email-address
                                       :user/roles]))
(s/def :command/delete-user (s/keys :req [:user/id]))

(s/def :query/current-user (s/and map? empty?))
(s/def :query/users (s/and map? empty?))
(s/def :query/user (s/keys :req [:user/id]))
(s/def :query/authorisations (s/and map? empty?))

(s/def :request/metadata (s/and map? empty?))
(s/def :request/command (s/and
                         (s/map-of keyword? map? :min-count 1)
                         (s/keys :opt-un [:command/initialise-authorisation-attempt
                                          :command/finalise-authorisation-attempt
                                          :command/deauthorise
                                          :command/add-user
                                          :command/delete-user])))
(s/def :request/query (s/and
                       (s/map-of keyword? map? :min-count 1)
                       (s/keys :opt-un [:query/current-user
                                        :query/users
                                        :query/user
                                        :query/authorisations])))
(s/def :request/body-params (s/and
                             (s/map-of keyword? map? :min-count 1)
                             (s/keys :opt-un [:request/metadata
                                              :request/command
                                              :request/query])))

(s/def :metadata/current-user-id (s/nilable :user/id))
(s/def :metadata/id-resolution (s/map-of :user/id :user/id))

(s/def :response/metadata (s/keys :req-un[:metadata/current-user-id]
                                  :opt-un [:metadata/id-resolution]))
(s/def :response/users (s/map-of :user/id
                                 (s/keys :opt [:user/id
                                               :user/name
                                               :user/email-address
                                               :user/roles
                                               :user/created-at
                                               :user/deleted-at])))
(s/def :response/authorisations (s/map-of :authorisation/id
                                          (s/keys :opt [:authorisation/id
                                                        :user/id
                                                        :authorisation/phrase
                                                        :authorisation/created-at
                                                        :authorisation/granted-at])))
(s/def :response/body (s/keys :req-un [:response/metadata
                                       :response/users
                                       :response/authorisations]))
