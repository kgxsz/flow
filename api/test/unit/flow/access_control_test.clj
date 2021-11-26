(ns flow.access-control-test
  (:require [flow.access-control :refer :all]
            [flow.dummy :as d]
            [clojure.test :refer :all]))

(def authorisation (first d/authorisations))

(deftest test-select-accessible-queries

  (testing "Returns only the default accessible queries when there is no current user."
    (is (= {:current-user {}}
           (select-accessible-queries
            {:current-user {}
             :users {}
             :user {}
             :authorisations {}}
            nil))))

  (testing "Returns the default and role accessible queries depending on the current user's role."
    (is (= {:current-user {}
            :users {}
            :user {}}
           (select-accessible-queries
            {:current-user {}
             :users {}
             :user {}
             :authorisations {}}
            {:user/roles #{:customer}})))
    (is (= {:current-user {}
            :users {}
            :user {}
            :authorisations {}}
           (select-accessible-queries
            {:current-user {}
             :users {}
             :user {}
             :authorisations {}}
            {:user/roles #{:customer :admin}})))))


(deftest test-select-accessible-commands

  (testing "Returns only the default accessible commands when there is no current user."
    (is (= {:initialise-authorisation-attempt {}
            :finalise-authorisation-attempt {}}
           (select-accessible-commands
            {:initialise-authorisation-attempt {}
             :finalise-authorisation-attempt {}
             :deauthorise {}
             :add-user {}
             :delete-user {}}
            nil))))

  (testing "Returns the default and role accessible commands depending on the current user's role."
    (is (= {:initialise-authorisation-attempt {}
            :finalise-authorisation-attempt {}
            :deauthorise {}}
           (select-accessible-commands
            {:initialise-authorisation-attempt {}
             :finalise-authorisation-attempt {}
             :deauthorise {}
             :add-user {}
             :delete-user {}}
            {:user/roles #{:customer}})))
    (is (= {:initialise-authorisation-attempt {}
            :finalise-authorisation-attempt {}
            :deauthorise {}
            :add-user {}
            :delete-user {}}
           (select-accessible-commands
            {:initialise-authorisation-attempt {}
             :finalise-authorisation-attempt {}
             :deauthorise {}
             :add-user {}
             :delete-user {}}
            {:user/roles #{:customer :admin}})))))


(deftest test-select-accessible-user-keys

  (testing "Returns only the default accessible keys when there is no current user."
    (is (= {} (select-accessible-user-keys (first d/users) nil))))

  (testing "Returns the owner accessible keys when the current user owns the entity."
    (is (= (first d/users)
           (select-accessible-user-keys
            (first d/users)
            (first d/users)))))

  (testing "Returns the role accessible keys depending on the current user's role."
    (is (= {:user/created-at #inst "2021-04-02T19:58:19.213-00:00"
            :user/deleted-at nil
            :user/id #uuid "19f3c785-cf5f-530b-841d-6161400e6793"
            :user/name "Johnson"}
           (select-accessible-user-keys
            (first d/users)
            (nth d/users 2))))
    (is (= (first d/users)
           (select-accessible-user-keys
            (first d/users)
            (second d/users))))))


(deftest test-select-accessible-authorisation-keys

  (testing "Returns only the default accessible keys when there is no current user."
    (is (= {} (select-accessible-authorisation-keys (first d/authorisations) nil))))

  (testing "Returns the owner accessible keys when the current user owns the entity."
    (is (= {}
           (select-accessible-authorisation-keys
            (first d/authorisations)
            (first d/users)))))

  (testing "Returns the role accessible keys depending on the current user's role."
    (is (= {}
           (select-accessible-authorisation-keys
            (first d/authorisations)
            (nth d/users 2))))
    (is (= (first d/authorisations)
           (select-accessible-authorisation-keys
            (first d/authorisations)
            (second d/users))))))
