(ns flow.entity.user-test
  (:require [flow.entity.user :refer :all]
            [flow.specifications :as s]
            [clojure.test :refer :all]))


(deftest test-id

  (testing "Returns a deterministic id based on the email address provided."
    (is (= #uuid "24f6930e-c076-5294-ac7d-b3327dd9e919" (id "hello@world.com")))))
