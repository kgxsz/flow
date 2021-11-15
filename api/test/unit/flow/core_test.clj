(ns flow.core-test
  (:require [flow.core :refer :all]
            [flow.query :as query]
            [flow.command :as command]
            [clojure.test :refer :all]))


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


(deftest test-cleanse-metadata

  (testing "Returns only the metadata associated with ID resolution and next offsets."
    (is (= {:users {"some-id" {:hello "world"}}
            :authorisations {"some-id" {:hello "world"}
                             "some-other-id" {:hello "world"}}
            :metadata {:hello "world"
                       :something "something"}
            :session {:hello "world"
                      :something "something"}}
           (cleanse-metadata {:query {:some-query {}
                                  :some-other-query {}}
                          :metadata {:something "something"}
                          :session {:something "something"}})))))
