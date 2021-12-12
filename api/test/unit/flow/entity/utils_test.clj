(ns flow.entity.utils-test
  (:require [flow.entity.utils :refer :all]
            [flow.specifications :as s]
            [clojure.test :refer :all]))


(deftest test-index-entities

  (testing "Returns the indexed entities that contain the provided key."
    (is (= {1 {:entity/x 1}
            2 {:entity/x 2}
            3 {:entity/x 3}}
           (index-entities
            :entity/x
            [{:entity/x 1}
             {:entity/x 2}
             {:entity/x 3}])))
    (is (= {1 {:entity/x 1}
            3 {:entity/x 3}}
           (index-entities
            :entity/x
            [{:entity/x 1}
             {:entity/y 2}
             {:entity/x 3}])))
    (is (= {}
           (index-entities
            :entity/x
            [{:entity/y 1}
             {:entity/y 2}
             {:entity/y 3}]))))

  (testing "Returns the indexed entities that are non nil."
    (is (= {1 {:entity/x 1}
            3 {:entity/x 3}}
           (index-entities
            :entity/x
            [{:entity/x 1}
             nil
             {:entity/x 3}])))
    (is (= {}
           (index-entities
            :entity/x
            [nil
             nil
             nil])))))
