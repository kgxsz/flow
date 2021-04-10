(ns flow.entity.utils-test
  (:require [flow.entity.utils :refer :all]
            [clojure.test :refer :all]))


(def entity {:user/id 1
             :entity/x "x"
             :entity/y "y"
             :entity/z "z"})


(deftest test-filter-sanctioned-keys

  (testing "Returns an entity filtered on the default sanctioned keys when there's no current user."
    (is (= {:entity/x "x"
            :entity/z "z"}
           (filter-sanctioned-keys
            {:default #{:entity/x :entity/z}
             :owner #{}
             :role {}}
            nil
            entity)))
    (is (= {}
           (filter-sanctioned-keys
            {:default #{:entity/w}
             :owner #{}
             :role {}}
            nil
            entity)))
    (is (= {}
           (filter-sanctioned-keys
            {:default #{}
             :owner #{}
             :role {}}
            nil
            entity))))

  (testing "Returns an entity filtered on the default sanctioned keys when the current user does not own the entity."
    (is (= {:entity/x "x"
            :entity/z "z"}
           (filter-sanctioned-keys
            {:default #{:entity/x :entity/z}
             :owner #{}
             :role {}}
            {:user/id 2
             :user/roles #{}}
            entity)))
    (is (= {}
           (filter-sanctioned-keys
            {:default #{:entity/w}
             :owner #{}
             :role {}}
            {:user/id 2}
            entity)))
    (is (= {}
           (filter-sanctioned-keys
            {:default #{}
             :owner #{}
             :role {}}
            {:user/id 2
             :user/roles #{}}
            entity))))

  (testing "Returns an entity filtered on the owner's sanctioned keys when the current user owns the entity."
    (is (= {:entity/x "x"
            :entity/z "z"}
           (filter-sanctioned-keys
            {:default #{}
             :owner #{:entity/x :entity/z}
             :role {}}
            {:user/id 1
             :user/roles #{}}
            entity)))
    (is (= {}
           (filter-sanctioned-keys
            {:default #{}
             :owner #{:entity/w}
             :role {}}
            {:user/id 1
             :user/roles #{}}
            entity)))
    (is (= {}
           (filter-sanctioned-keys
            {:default #{}
             :owner #{}
             :role {}}
            {:user/id 1
             :user/roles #{}}
            entity))))

  (testing "Returns an entity filtered on the roles' sanctioned keys matching the current user's roles."
    (is (= {:entity/x "x"
            :entity/z "z"}
           (filter-sanctioned-keys
            {:default #{}
             :owner #{}
             :role {:a #{:entity/x :entity/z}}}
            {:user/id 2
             :user/roles #{:a}}
            entity)))
    (is (= {:entity/x "x"
            :entity/z "z"}
           (filter-sanctioned-keys
            {:default #{}
             :owner #{}
             :role {:a #{:entity/x}
                    :b #{:entity/z}}}
            {:user/id 2
             :user/roles #{:a :b}}
            entity)))
    (is (= {:entity/x "x"}
           (filter-sanctioned-keys
            {:default #{}
             :owner #{}
             :role {:a #{:entity/x :entity/w}}}
            {:user/id 2
             :user/roles #{:a :b}}
            entity)))
    (is (= {}
           (filter-sanctioned-keys
            {:default #{}
             :owner #{}
             :role {:a #{}}}
            {:user/id 2
             :user/roles #{:a}}
            entity)))
    (is (= {}
           (filter-sanctioned-keys
            {:default #{}
             :owner #{}
             :role {:a #{:entity/x}}}
            {:user/id 2
             :user/roles #{:b}}
            entity)))

(testing "Returns an entity filtered on a combination of default, the owner's, and roles' sanctioned keys."
    (is (= {:entity/x "x"
            :entity/y "y"
            :entity/z "z"}
           (filter-sanctioned-keys
            {:default #{:entity/x}
             :owner #{:entity/y}
             :role {:a #{:entity/z :entity/x}}}
            {:user/roles #{:a}
             :user/id 1}
            entity)))
    (is (= {:entity/x "x"}
           (filter-sanctioned-keys
            {:default #{:entity/x}
             :owner #{:entity/y}
             :role {:a #{:entity/z :entity/x}}}
            {:user/roles #{:b}
             :user/id 2}
            entity))))))
