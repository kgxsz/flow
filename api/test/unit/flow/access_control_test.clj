(ns flow.access-control-test
  (:require [flow.access-control :refer :all]
            [clojure.test :refer :all]))


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
