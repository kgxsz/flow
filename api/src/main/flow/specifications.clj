(ns flow.specifications
  (:require [flow.utils :as u]
            [clojure.spec.alpha :as s]
            [clojure.string :as string]))


;; Common
(s/def :common/email-address (s/and u/email-address?
                                    (partial u/constrained-string? 250)))


;; User
(s/def :user/id uuid?)
(s/def :user/name (s/and u/sanitised-string?
                         (partial u/constrained-string? 250)))
(s/def :user/email-address :common/email-address)
(s/def :user/role #{:customer :admin})
(s/def :user/roles (s/coll-of :user/role :kind set? :min-count 1))
(s/def :user/created-at inst?)
(s/def :user/deleted-at (s/nilable inst?))


;; Authorisation
(s/def :authorisation/id uuid?)
(s/def :authorisation/phrase (s/and u/sanitised-string?
                                    (partial u/constrained-string? 250)))
(s/def :authorisation/created-at inst?)
(s/def :authorisation/granted-at (s/nilable inst?))


;; DB
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

(s/def :db/entity-partition (s/or :user #(string/starts-with? % "user:")
                                  :authorisation #(string/starts-with? % "authorisation:")))



;; Email
(s/def :email/html (partial u/constrained-string? 1000))
(s/def :email/text (partial u/constrained-string? 1000))
(s/def :email/subject (partial u/constrained-string? 50))
(s/def :email/email-address :common/email-address)


;; Command
(s/def :command/initialise-authorisation-attempt (s/keys :req [:user/email-address]))
(s/def :command/finalise-authorisation-attempt (s/keys :req [:user/email-address
                                                             :authorisation/phrase]))
(s/def :command/deauthorise (s/and map? empty?))
(s/def :command/add-user (s/keys :req [:user/id
                                       :user/name
                                       :user/email-address
                                       :user/roles]))
(s/def :command/delete-user (s/keys :req [:user/id]))
(s/def :command/method #{:initialise-authorisation-attempt
                         :finalise-authorisation-attempt
                         :deauthorise
                         :add-user
                         :delete-user})


;; Query
(s/def :query/current-user (s/and map? empty?))
(s/def :query/users (s/and map? empty?))
(s/def :query/user (s/keys :req [:user/id]))
(s/def :query/authorisations (s/and map? empty?))
(s/def :query/method #{:current-user
                      :users
                      :user
                      :authorisations})


;; Metadata
(s/def :metadata/id-resolution (s/map-of :user/id :user/id))


;; Session
(s/def :session/current-user-id (s/nilable :user/id))


;; Request
(s/def :request/command (s/and (s/map-of :command/method any?)
                               (s/keys :opt-un [:command/initialise-authorisation-attempt
                                                :command/finalise-authorisation-attempt
                                                :command/deauthorise
                                                :command/add-user
                                                :command/delete-user])))
(s/def :request/query (s/and (s/map-of :query/method any?)
                             (s/keys :opt-un [:query/current-user
                                              :query/users
                                              :query/user
                                              :query/authorisations])))
(s/def :request/metadata (s/and map? empty?))
(s/def :request/session (s/and map? empty?))
(s/def :request/body-params (s/and (s/map-of #{:command :query :metadata :session} any?)
                                   (s/keys :req-un [:request/command
                                                    :request/query
                                                    :request/session
                                                    :request/metadata])))


;; Response
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
(s/def :response/metadata (s/keys :opt-un [:metadata/id-resolution]))
(s/def :response/session (s/keys :req-un [:session/current-user-id]))
(s/def :response/body (s/and (s/map-of #{:users :authorisations :metadata :session} any?)
                             (s/keys :req-un [:response/users
                                              :response/authorisations
                                              :respone/metadata
                                              :response/session])))
