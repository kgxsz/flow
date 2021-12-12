(ns flow.specifications
  (:require [cljs.spec.alpha :as s]
            [clojure.string :as string]
            [flow.utils :as u]
            [medley.core :as medley]))

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


;; Routing
(s/def :routing/route #{:home
                        :admin.users
                        :admin.authorisations
                        :unknown})
(s/def :routing/current-route :routing/route)
(s/def :routing/next-route :routing/route)

(s/def :routing.parameters/route map?)
(s/def :routing.parameters/query map?)
(s/def :routing/parameters (s/keys :req-un [:routing.parameters/route
                                            :routing.parameters/query]))
(s/def :routing/current-parameters :routing/parameters)
(s/def :routing/next-parameters :routing/parameters)


;; Session
(s/def :session/current-user-id (s/nilable uuid?))


;; Entities
(s/def :entities/users (s/map-of :user/id
                                 (s/keys :req [:user/id
                                               :user/name
                                               :user/email-address
                                               :user/roles
                                               :user/created-at
                                               :user/deleted-at])))
(s/def :entities/authorisations (s/map-of :authorisation/id
                                          (s/keys :req [:authorisation/id
                                                        :user/id
                                                        :authorisation/phrase
                                                        :authorisation/created-at
                                                        :authorisation/granted-at])))


;; DB
(s/def :db/routing (s/keys :opt-un [:routing/current-route
                                    :routing/current-parameters
                                    :routing/next-route
                                    :routing/next-parameters]))
(s/def :db/session (s/keys :req-un [:session/current-user-id]))
(s/def :db/entities (s/keys :req-un [:entities/users
                                     :entities/authorisations]))


;; App
(s/def :app/db (s/keys :req-un [:db/routing]
                       :opt-un [:db/entities
                                :db/session]))
