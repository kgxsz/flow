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

  (testing "Returns the spec key when the entity type is known."
    (is (= :db/user (entity-specification :user))))

  (testing "Returns the spec key when the entity type is unknown."
    (is (= :db/hello-world (entity-specification :hello-world)))))


(deftest test-entity-partition

  (testing "Returns the entity partition when the entity type is known."
    (is (= "user:19f3c785-cf5f-530b-841d-6161400e6793"
           (entity-partition entity-type entity-id))))

  (testing "Returns the entity partition when the entity type is unknown."
    (is (= "hello-world:19f3c785-cf5f-530b-841d-6161400e6793"
           (entity-partition :hello-world entity-id)))))


(deftest test-fetch-entity

  (testing "Returns nil when the underlying entity does not exist."
    (with-redefs [faraday/get-item (constantly nil)]
      (is (nil? (fetch-entity entity-type entity-id)))))

  (testing "Returns the entity when the underlying entity exists."
    (with-redefs [faraday/get-item (constantly {:entity entity})]
      (is (= entity (fetch-entity entity-type entity-id))))))


(deftest test-fetch-entities

  (testing "Returns an empty vector when no underlying entities exist."
    (with-redefs [faraday/scan (constantly [])]
      (is (= [] (fetch-entities :authorisations)))))

  (testing "Returns a vecotr of entities when any underlying entities exist."
    (with-redefs [faraday/scan (constantly [{:entity entity}
                                            {:entity entity}])]
      (is (= [entity entity] (fetch-entities :authorisation))))))


(deftest test-put-entity!

  (testing "Throws an exception when the entity provided already exists."
    (with-redefs [faraday/get-item (constantly {:entity entity})
                  faraday/put-item (constantly nil)]
      (is (thrown? IllegalStateException
                   (put-entity! entity-type entity-id entity)))))

  (testing "Throws an exception whe the entity provided violates specification."
    (with-redefs [faraday/get-item (constantly nil)
                  faraday/put-item (constantly nil)]
      (is (thrown? IllegalStateException
                   (put-entity! entity-type entity-id {})))))

  (testing "Returns the entity ID when the operation is executed successfully."
    (with-redefs [faraday/get-item (constantly nil)
                  faraday/put-item (constantly nil)]
      (is (= entity-id (put-entity! entity-type entity-id entity))))))


(deftest test-mutate-entity!

  (testing "Throws an exception when the entity provided does not exist."
    (with-redefs [faraday/get-item (constantly nil)
                  faraday/update-item (constantly nil)]
      (is (thrown? IllegalStateException
                   (mutate-entity! entity-type entity-id identity)))))

  (testing "Throws an exception when the entity mutation doesn't adhere to specification."
    (with-redefs [faraday/get-item (constantly {:entity entity})
                  faraday/update-item (constantly nil)]
      (is (thrown? IllegalStateException
                   (mutate-entity! entity-type entity-id (constantly {}))))))

  (testing "Returns the entity ID when the operation is executed successfully."
    (with-redefs [faraday/get-item (constantly {:entity entity})
                  faraday/update-item (constantly nil)]
      (is (= entity-id (mutate-entity! entity-type entity-id identity))))))
