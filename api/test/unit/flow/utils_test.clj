(ns flow.utils-test
  (:require [flow.utils :refer :all]
            [clojure.test :refer :all]))


(deftest test-index

  (testing "The specified key either does not exist or is nil."
    (is (= {} (index :x {:x nil})))
    (is (= {} (index :z {:x :y}))))

  (testing "The specified key can be extracted."
    (is (= {:y {:x :y}} (index :x {:x :y})))
    (is (= {"y" {:x "y"}} (index :x {:x "y"})))))


(deftest test-constained-string?

  (testing "The string length exceeds n."
    (is (false? (constrained-string? 4 "Hello"))))

  (testing "The string length equals n."
    (is (true? (constrained-string? 5 "Hello"))))

  (testing "The string length subceeds n."
    (is (true? (constrained-string? 5 "Hello"))))

  (testing "The string is blank."
    (is (false? (constrained-string? 5 "")))))


(deftest test-sanitised-string?

  (testing "The string contains whitespaces."
    (is (false? (sanitised-string? " Hello")))
    (is (false? (sanitised-string? "Hello ")))
    (is (false? (sanitised-string? "Hello World"))))

  (testing "The string contains newlines or carriage returns."
    (is (false? (sanitised-string? "Hello\nWorld")))
    (is (false? (sanitised-string? "Hello\n")))
    (is (false? (sanitised-string? "Hello\rWorld")))
    (is (false? (sanitised-string? "\rHello")))))


(deftest test-email-address?

  (testing "The string contains whitespaces, newlines, or carriage returns."
    (is (false? (email-address? " hello@world.com")))
    (is (false? (email-address? "hello@world.com ")))
    (is (false? (email-address? "hel lo@world.com")))
    (is (false? (email-address? "hello@\nworld.com")))
    (is (false? (email-address? "hello@world.com\r"))))

  (testing "The string does not adhere to an email address pattern."
    (is (false? (email-address? "hello")))
    (is (false? (email-address? "hello@.com")))
    (is (false? (email-address? "@world.com")))
    (is (false? (email-address? "hello@world")))
    (is (false? (email-address? ".hello@world")))
    (is (false? (email-address? "hello@world.")))
    (is (false? (email-address? "Hello@World.com"))))

  (testing "The string adheres to an email address pattern."
    (is (true? (email-address? "hello@world.com")))
    (is (true? (email-address? "hello.world@hello.world.com")))))
