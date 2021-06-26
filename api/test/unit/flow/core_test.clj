(ns flow.core-test
  (:require [flow.entity.user :as user]
            [flow.core :refer :all]
            [flow.query :as query]
            [flow.command :as command]
            [flow.specifications :as s]
            [flow.utils :as u]
            [medley.core :as medley]
            [muuntaja.core :as muuntaja]
            [clojure.test :refer :all]))


(def request
  {:request-method :post
   :uri "/"
   :body "[\"^ \",\"~:query\",[\"^ \",\"~:users\",[\"^ \"]]]"
   :headers {"origin" "https://localhost:8080"
             "access-control-request-method" "POST"
             "content-type" "application/transit+json"
             "accept" "application/transit+json"}})

(def response
  {:status 200
   :headers {}
   :body {:users {}
          :authorisations {}}})

(def user {:user/id #uuid "19f3c785-cf5f-530b-841d-6161400e6793"
           :user/email-address "j.mcjohnson@gmail.com"
           :user/name "Johnson"
           :user/roles #{:customer}
           :user/created-at #inst "2021-04-02T19:58:19.213-00:00"
           :user/deleted-at nil})


(def authorisations [{:authorisation/id #uuid "22a3c785-0f5f-530b-841d-7761400e6793"
                      :user/id #uuid "19f3c785-cf5f-530b-841d-6161400e6793"
                      :authorisation/phrase "amount-addition-harbor",
                      :authorisation/created-at #inst "2021-04-03T11:21:46.894-00:00",
                      :authorisation/granted-at nil}
                     {:authorisation/id #uuid "31f3c785-0f5f-530b-841d-7761400e6793"
                      :user/id #uuid "00f3c785-cf5f-530b-841d-6161400e6793"
                      :authorisation/phrase "concrete-tree-bridge",
                      :authorisation/created-at #inst "2021-04-03T11:21:46.894-00:00",
                      :authorisation/granted-at nil}])


(deftest test-handle-command

  (testing "Returns any metadata and session returned from commands merged into the metadata
            and session provided."
    (with-redefs [command/handle (constantly {:metadata {:hello "world"}
                                              :session {:hello "world"}})]
      (is (= {:query {}
              :metadata {:hello "world"
                         :something "something"}
              :session {:hello "world"
                        :something "something"}}
             (handle-command {:command {:some-command {}
                                        :some-other-command {}}
                              :query {}
                              :metadata {:something "something"}
                              :session {:something "something"}}))))))


(deftest test-resolve-ids

  (testing "Returns the query unchanged when no resolution map is provided."
    (is (= {:query {:some-query {:entity-ids ["some-id" "another-id"]}
                    :some-other-query {:entity-id "yet-another-id"}}
            :metadata {}
            :session {}}
           (resolve-ids {:query {:some-query {:entity-ids ["some-id" "another-id"]}
                                 :some-other-query {:entity-id "yet-another-id"}}
                         :metadata {}
                         :session {}}))))

  (testing "Returns the query with IDs resolved according to the resolution map provided."
    (is (= {:query {:some-query {:entity-ids ["some-resolved-id" "another-id"]}
                    :some-other-query {:entity-id "yet-another-resolved-id"}}
            :metadata {:id-resolution {"some-id" "some-resolved-id"
                                       "yet-another-id" "yet-another-resolved-id"}}
            :session {}}
           (resolve-ids {:query {:some-query {:entity-ids ["some-id" "another-id"]}
                                 :some-other-query {:entity-id "yet-another-id"}}
                         :metadata {:id-resolution {"some-id" "some-resolved-id"
                                                    "yet-another-id" "yet-another-resolved-id"}}
                         :session {}})))))


(deftest test-handle-query

  (testing "Returns any metadata and session returned from queries merged into the metadata
            and session provided, and returns any entities returned from the queries."
    (with-redefs [query/handle (constantly {:users {"some-id" {:hello "world"}}
                                            :authorisations {"some-id" {:hello "world"}
                                                             "some-other-id" {:hello "world"}}
                                            :metadata {:hello "world"}
                                            :session {:hello "world"}})]
      (is (= {:users {"some-id" {:hello "world"}}
              :authorisations {"some-id" {:hello "world"}
                               "some-other-id" {:hello "world"}}
              :metadata {:hello "world"
                         :something "something"}
              :session {:hello "world"
                        :something "something"}}
             (handle-query {:query {:some-query {}
                                    :some-other-query {}}
                            :metadata {:something "something"}
                            :session {:something "something"}}))))))
