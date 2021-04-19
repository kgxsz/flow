(ns flow.entity.utils-test
  (:require [flow.entity.utils :refer :all]
            [flow.specifications :as s]
            [clojure.test :refer :all]))


(def entity {:user/id 1
             :entity/x "x"
             :entity/y "y"
             :entity/z "z"})

(def current-user {:user/id 1
                   :user/roles #{:a :b}})


(deftest test-index-entities

  (testing "Returns the indexed entities when the key and entity exist."
    (is (= {1 {:entity/x 1}
            2 {:entity/x 2}
            3 {:entity/x 3}}
           (index-entities
            :entity/x
            [{:entity/x 1}
             {:entity/x 2}
             {:entity/x 3}]))))

  (testing "Omits any entity when its key doesn't exist."
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


  (testing "Omits any entity that doesn't exist."
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


(deftest test-select-default-accessible-keys

  (testing "Selects the defauls accessible keys provided."
    (is (= {:entity/x "x" :entity/y "y"}
           (select-default-accessible-keys
            [:entity/x :entity/y]
            entity))))

  (testing "Returns an empty map when there are no default accessbile keys."
    (is (= {}
           (select-default-accessible-keys
            []
            entity)))))


(deftest test-select-owner-accessible-keys

  (testing "Selects the owner accessible keys when the current user owns the entity."
    (is (= {:entity/x "x" :entity/y "y"}
           (select-owner-accessible-keys
            [:entity/x :entity/y]
            current-user
            entity))))

  (testing "Returns an empty map when the current user doesn't own the entity."
    (is (= {}
           (select-owner-accessible-keys
            [:entity/x :entity/y]
            (assoc current-user :user/id 2)
            entity))))

  (testing "Returns an empty map when there's no current user."
    (is (= {}
           (select-owner-accessible-keys
            [:entity/x :entity/y]
            nil
            entity)))))


(deftest test-select-role-accessible-keys

  (testing "Selects the role accessible keys corresponding to the current user's roles."
    (is (= {:entity/x "x" :entity/y "y"}
           (select-role-accessible-keys
            {:a [:entity/x] :b [:entity/y]}
            current-user
            entity)))
    (is (= {:entity/y "y" :entity/z "z"}
           (select-role-accessible-keys
            {:a [] :b [:entity/y :entity/z] :c [:entity/x]}
            current-user
            entity))))

  (testing "Returns an empty map when there are no coresponding role accessible keys."
    (is (= {}
           (select-role-accessible-keys
            {:a [:entity/x]}
            (assoc current-user :user/roles #{})
            entity)))
    (is (= {}
           (select-role-accessible-keys
            {:a [] :b [:entity/y]}
            (assoc current-user :user/roles #{:a})
            entity)))
    (is (= {}
           (select-role-accessible-keys
            {:a [:entity/x] :b [:entity/y]}
            (assoc current-user :user/roles #{:c})
            entity))))

  (testing "Returns an empty map when there's no current user."
    (is (= {}
           (select-role-accessible-keys
            {:a [:entity/x]}
            nil
            entity)))))
