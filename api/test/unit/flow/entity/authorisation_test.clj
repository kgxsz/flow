(ns flow.entity.authorisation-test
  (:require [flow.entity.authorisation :refer :all]
            [flow.specifications :as s]
            [clojure.test :refer :all]))


(deftest test-id

  (testing "Returns a deterministic id based on the user id and phrase provided."
    (is (= #uuid "b01f64d8-9fac-5c6a-af8e-f31eb68aa5b8"
           (id #uuid "66f3c785-cf5f-530b-8f1d-6161400e679d" "hello-world-there")))))
