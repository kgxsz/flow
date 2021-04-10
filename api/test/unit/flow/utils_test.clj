(ns flow.utils-test
  (:require [flow.utils :refer :all]
            [clojure.test :refer :all]))


(deftest test-index

  (testing "Returns an empty map when the specified key doesn't exist."
    (is (= {} (index :z {:x :y}))))

  (testing "Returns an empty map when the specified key is nil."
    (is (= {} (index :x {:x nil}))))

  (testing "Returns the indexed map when the specified key can be extracted."
    (is (= {:y {:x :y}} (index :x {:x :y})))
    (is (= {"y" {:x "y"}} (index :x {:x "y"})))))


(deftest test-constained-string?

  (testing "Returns false when the string length exceeds n."
    (is (false? (constrained-string? 4 "Hello"))))

  (testing "Returns false when the string is blank."
    (is (false? (constrained-string? 5 ""))))

  (testing "Returns true when the string length equals n."
    (is (true? (constrained-string? 5 "Hello"))))

  (testing "Returns true when the string length subceeds n."
    (is (true? (constrained-string? 5 "Hello")))))


(deftest test-sanitised-string?

  (testing "Returns false when the string contains whitespaces."
    (is (false? (sanitised-string? " Hello")))
    (is (false? (sanitised-string? "Hello ")))
    (is (false? (sanitised-string? "Hello World"))))

  (testing "Returns false when the string contains newlines or carriage returns."
    (is (false? (sanitised-string? "Hello\nWorld")))
    (is (false? (sanitised-string? "Hello\n")))
    (is (false? (sanitised-string? "Hello\rWorld")))
    (is (false? (sanitised-string? "\rHello"))))

  (testing "Returns true when the string is sanitised."
    (is (true? (sanitised-string? "Hello")))))


(deftest test-email-address?

  (testing "Returns false when the string contains whitespaces."
    (is (false? (email-address? " hello@world.com")))
    (is (false? (email-address? "hello@world.com ")))
    (is (false? (email-address? "hel lo@world.com"))))

  (testing "Returns false when the string contains newlines or carriage returns."
    (is (false? (email-address? "hello@\nworld.com")))
    (is (false? (email-address? "hello@world.com\r"))))

  (testing "Returns false when the string does not adhere to an email address pattern."
    (is (false? (email-address? "hello")))
    (is (false? (email-address? "hello@.com")))
    (is (false? (email-address? "@world.com")))
    (is (false? (email-address? "hello@world")))
    (is (false? (email-address? ".hello@world")))
    (is (false? (email-address? "hello@world.")))
    (is (false? (email-address? "Hello@World.com"))))

  (testing "Returns true when the string adheres to an email address pattern."
    (is (true? (email-address? "hello@world.com")))
    (is (true? (email-address? "hello.world@hello.world.com")))))


(deftest test-validate?

  (testing "Returns the data when the data provided doesn't violate specification."
    (is (= "hello@world.com"
           (validate
            :email/email-address
            "hello@world.com")))
    (is (= {:query {:current-user {}}}
           (validate
            :request/body-params
            {:query {:current-user {}}}))))

  (testing "Throws an exception when the data provided violates specification."
    (is (thrown? IllegalStateException
                 (validate
                  :email/email-address
                  "@world.com")))
    (is (thrown? IllegalStateException
                 (validate
                  :request/body-params
                  {:query {:current-user {:hello "world"}}})))))
