(ns flow.domain.user-management-test
  (:require [flow.domain.user-management :refer :all]
            [flow.specifications :as s]
            [flow.dummy :as d]
            [clojure.test :refer :all]
            [clj-time.coerce :as t.coerce]
            [clj-time.core :as t]))


(def user (first d/users))


(deftest test-delete

  (testing "Returns the user with the deleted-at field updated from nil to
            an instant representing now."
    (let [now (t/date-time 2021 4 20)
          user' (assoc user :user/deleted-at (t.coerce/to-date now))]
      (with-redefs [t/now (constantly now)]
        (is (= user' (delete user))))))

  (testing "Returns the user unchanged if the user was previously deleted."
    (let [now (t/date-time 2021 4 20)
          before #inst "2021-04-10T00:00:00.000-00:00"
          user' (assoc user :user/deleted-at before)]
      (with-redefs [t/now (constantly now)]
        (is (= user' (delete user')))))))


(deftest test-admin?

  (testing "Returns true when the current user has an admin role."
    (is (true? (admin? {:user/roles #{:admin}}))))

  (testing "Returns true when the current user does not have an admin role."
    (is (false? (admin? {:user/roles #{:customer}}))))

  (testing "Returns false when the current user does not exist."
    (is (false? (admin? nil)))))
