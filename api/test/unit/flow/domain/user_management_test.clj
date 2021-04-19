(ns flow.domain.user-management-test
  (:require [flow.domain.user-management :refer :all]
            [flow.specifications :as s]
            [clojure.test :refer :all]
            [clj-time.coerce :as t.coerce]
            [clj-time.core :as t]))


(def user {:user/id #uuid "19f3c785-cf5f-530b-841d-6161400e6793"
           :user/email-address "j.mcjohnson@gmail.com"
           :user/name "Johnson"
           :user/roles #{:customer}
           :user/created-at #inst "2021-04-02T19:58:19.213-00:00"
           :user/deleted-at nil})


(deftest test-delete

  (testing "Returns the user with the deleted-at field updated from nil to
            an instant representing now."
    (let [now (t.coerce/to-date (t/now))
          user' (assoc user :user/deleted-at now)]
      (is (= user' (delete user)))))

  (testing "Returns the user unchanged if the user was previously deleted."
    (let [before #inst "2000-01-01T00:00:00.000-00:00"
          user' (assoc user :user/deleted-at before)]
      (is (= user' (delete user'))))))


(deftest test-admin?

  (testing "Returns true when the current user has an admin role."
    (is (true? (admin? {:user/roles #{:admin}}))))

  (testing "Returns true when the current user does not have an admin role."
    (is (false? (admin? {:user/roles #{:customer}}))))

  (testing "Returns false when the current user does not exist."
    (is (false? (admin? nil)))))


(deftest test-exists?

  (testing "Returns true when the current user has an admin role."
    (is (true? (admin? {:user/roles #{:admin}}))))

  (testing "Returns false when the current user does not exist."
    (is (false? (admin? nil)))))
