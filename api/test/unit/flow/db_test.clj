(ns flow.db-test
  (:require [flow.db :refer :all]
            [flow.specifications :as s]
            [flow.dummy :as d]
            [taoensso.faraday :as faraday]
            [clojure.test :refer :all]
            [slingshot.test :refer :all]))


(def user (first d/users))

(def authorisation (first d/authorisations))

(deftest test-entity-specification

  (testing "Returns the spec key when the entity type is known."
    (is (= :db/user (entity-specification :user)))
    (is (= :db/authorisation (entity-specification :authorisation))))

  (testing "Returns the spec key when the entity type is unknown."
    (is (= :db/hello-world (entity-specification :hello-world)))))


(deftest test-entity-partition

  (testing "Returns the entity partition when the entity type is known."
    (is (= "user:19f3c785-cf5f-530b-841d-6161400e6793"
           (entity-partition :user (:user/id user))))
    (is (= "authorisation:31f3c785-0f5f-530b-841d-7761400e6793"
           (entity-partition :authorisation (:authorisation/id authorisation)))))

  (testing "An exception is thrown when the entity type is unknown."
    (= "hello-world:09f3c185-005f-530b-841d-1161400e6793"
       (entity-partition :hello-world "09f3c185-005f-530b-841d-1161400e6793"))))


(deftest test-fetch-entity

  (testing "Returns nil when the underlying entity does not exist."
    (with-redefs [faraday/get-item (constantly nil)]
      (is (nil? (fetch-entity :user (:user/id user))))))

  (testing "Returns the entity when the underlying entity exists."
    (with-redefs [faraday/get-item (constantly {:entity authorisation})]
      (is (= authorisation (fetch-entity :authorisation (:authorisation/id authorisation)))))))


(deftest test-fetch-entities

  (testing "Returns an empty vector when no underlying entities exist."
    (with-redefs [faraday/scan (constantly [])]
      (is (= [] (fetch-entities :authorisations 10 nil)))))

  (testing "Returns a vector of entities when any underlying entities exist."
    (with-redefs [faraday/scan (constantly [{:entity user} {:entity user}])]
      (is (= [user user] (fetch-entities :user 10 nil)))))

  (testing "Returns a vector with the number of entities less than or equal to the limit."
    (with-redefs [faraday/scan (constantly [{:entity user} {:entity user} {:entity user}])]
      (is (= 3 (count (fetch-entities :user 4 nil))))
      (is (= 3 (count (fetch-entities :user 3 nil))))
      (is (= 2 (count (fetch-entities :user 2 nil))))
      (is (= 1 (count (fetch-entities :user 1 nil))))))

  (testing "Returns a vector where the entities are offset."
    (with-redefs [faraday/scan (constantly [{:entity (nth d/users 1)} {:entity user} {:entity (nth d/users 2)}])]
      (is (= [(nth d/users 1) user (nth d/users 2)] (fetch-entities :user 3 nil)))
      (is (= [user (nth d/users 2)] (fetch-entities :user 3 (:user/id (nth d/users 1)))))
      (is (= [user] (fetch-entities :user 1 (:user/id (nth d/users 1)))))
      (is (= [(nth d/users 2)] (fetch-entities :user 3 (:user/id user))))
      (is (= [] (fetch-entities :user 3 (:user/id (nth d/users 2))))))))


(deftest test-create-entity!

  (testing "Throws an exception when the entity provided already exists."
    (with-redefs [faraday/get-item (constantly {:entity user})
                  faraday/put-item (constantly nil)]
      (is (thrown+? [:type :flow/internal-error]
                    (create-entity! :user (:user/id user) user)))))

  (testing "Throws an exception when the entity type provided violates specification."
    (with-redefs [faraday/get-item (constantly nil)
                  faraday/put-item (constantly nil)]
      (is (thrown+? [:type :flow/internal-error]
                    (create-entity! :hello-world (:authorisation/id authorisation) authorisation)))))

  (testing "Throws an exception when the entity provided violates specification."
    (with-redefs [faraday/get-item (constantly nil)
                  faraday/put-item (constantly nil)]
      (is (thrown+? [:type :flow/internal-error]
                    (create-entity! :user (:authorisation/id user) {})))
      (is (thrown+? [:type :flow/internal-error]
                    (create-entity! :user (:authorisation/id authorisation) authorisation)))))

  (testing "Returns the entity ID when the operation is executed successfully."
    (with-redefs [faraday/get-item (constantly nil)
                  faraday/put-item (constantly nil)]
      (is (= (:user/id user) (create-entity! :user (:user/id user) user))))))


(deftest test-mutate-entity!

  (testing "Throws an exception when the entity provided does not exist."
    (with-redefs [faraday/get-item (constantly nil)
                  faraday/update-item (constantly nil)]
      (is (thrown+? [:type :flow/internal-error]
                    (mutate-entity! :user (:user/id user) identity)))))

  (testing "Throws an exception when the entity type provided doesn't adhere to specification."
    (with-redefs [faraday/get-item (constantly {:entity authorisation})
                  faraday/update-item (constantly nil)]
      (is (thrown+? [:type :flow/internal-error]
                    (mutate-entity! :hello-world (:authorisation/id authorisation) identity)))))

  (testing "Throws an exception when the entity mutation doesn't adhere to specification."
    (with-redefs [faraday/get-item (constantly {:entity authorisation})
                  faraday/update-item (constantly nil)]
      (is (thrown+? [:type :flow/internal-error]
                    (mutate-entity! :authorisation (:authorisation/id authorisation) (constantly {}))))))

  (testing "Returns the entity ID when the operation is executed successfully."
    (with-redefs [faraday/get-item (constantly {:entity user})
                  faraday/update-item (constantly nil)]
      (is (= (:user/id user) (mutate-entity! :user (:user/id user) identity))))))
