(ns flow.db-test
  (:require [flow.db :refer :all]
            [taoensso.faraday :as faraday]
            [clojure.test :refer :all]))


(def entity-id #uuid "19f3c785-cf5f-530b-841d-6161400e6793")
(def entity-type :user)
(def entity {:user/id #uuid "19f3c785-cf5f-530b-841d-6161400e6793"
             :user/email-address "j.mcjohnson@gmail.com"
             :user/name "Johnson"
             :user/roles #{:customer}
             :user/created-at #inst "2021-04-02T19:58:19.213-00:00"
             :user/deleted-at nil})


(deftest test-entity-specification

  (testing "The entity type is known."
    (is (= :db/user (entity-specification :user))))

  (testing "The entity type is unknown."
    (is (= :db/hello-world (entity-specification :hello-world)))))


(deftest test-entity-partition

  (testing "The entity type is known."
    (is (= "user:19f3c785-cf5f-530b-841d-6161400e6793"
           (entity-partition entity-type entity-id))))

  (testing "The entity type is unknown."
    (is (= "hello-world:19f3c785-cf5f-530b-841d-6161400e6793"
           (entity-partition :hello-world entity-id)))))


(deftest test-fetch-entity

  (testing "The underlying entity does not exist."
    (with-redefs [faraday/get-item (constantly nil)]
      (is (nil? (fetch-entity entity-type entity-id)))))

  (testing "The underlying entity exists."
    (with-redefs [faraday/get-item (constantly {:entity entity})]
      (is (= entity (fetch-entity entity-type entity-id))))))


(deftest test-fetch-entities

  (testing "No underlying entities exist."
    (with-redefs [faraday/scan (constantly [])]
      (is (= [] (fetch-entities :authorisations)))))

  (testing "Some underlying entities exist."
    (with-redefs [faraday/scan (constantly [{:entity entity}
                                            {:entity entity}])]
      (is (= [entity entity] (fetch-entities :authorisation))))))


(deftest test-put-entity!

  (testing "The entity provided already exists."
    (with-redefs [faraday/get-item (constantly {:entity entity})
                  faraday/put-item (constantly nil)]
      (is (thrown? IllegalStateException
                   (put-entity! entity-type entity-id entity)))))

  (testing "The entity provided violates specification."
    (with-redefs [faraday/get-item (constantly nil)
                  faraday/put-item (constantly nil)]
      (is (thrown? IllegalStateException
                   (put-entity! entity-type entity-id {})))))

  (testing "The entity provided doesn't exist and adheres to specification."
    (with-redefs [faraday/get-item (constantly nil)
                  faraday/put-item (constantly nil)]
      (is (= entity-id (put-entity! entity-type entity-id entity))))))


(deftest test-mutate-entity!

  (testing "The entity provided does not exist."
    (with-redefs [faraday/get-item (constantly nil)
                  faraday/update-item (constantly nil)]
      (is (thrown? IllegalStateException
                   (mutate-entity! entity-type entity-id identity)))))

  (testing "The entity mutation doesn't adhere to specification."
    (with-redefs [faraday/get-item (constantly {:entity entity})
                  faraday/update-item (constantly nil)]
      (is (thrown? IllegalStateException
                   (mutate-entity! entity-type entity-id (constantly {}))))))

  (testing "The entity provided exists and its mutation adheres to specification."
    (with-redefs [faraday/get-item (constantly {:entity entity})
                  faraday/update-item (constantly nil)]
      (is (= entity-id (mutate-entity! entity-type entity-id identity))))))
