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


(deftest test-sanitised-string?

  (testing "The string length is considered."
    (is (false? (sanitised-string? 4 "Hello")))
    (is (true? (sanitised-string? 5 "Hello")))
    (is (true? (sanitised-string? 6 "Hello"))))

  (testing "The string contains whitespaces, newlines, or carriage returns."
    (is (false? (sanitised-string? 30 " Hello")))
    (is (false? (sanitised-string? 30 "Hello ")))
    (is (false? (sanitised-string? 30 "Hello World")))
    (is (false? (sanitised-string? 30 "Hello\nWorld")))
    (is (false? (sanitised-string? 30 "Hello\rWorld")))))


(deftest test-email-address?

  (testing "The string does not adhere to an email address pattern."
    (is (false? (email-address? "hello")))
    (is (false? (email-address? "hello@.com")))
    (is (false? (email-address? "@world.com")))
    (is (false? (email-address? "hello@world")))
    (is (false? (email-address? ".hello@world")))
    (is (false? (email-address? "hello@world.")))
    (is (false? (email-address? "Hello@World.com"))))

  (testing "The string contains whitespaces, newlines, or carriage returns."
    (is (false? (email-address? " hello@world.com")))
    (is (false? (email-address? "hello@world.com ")))
    (is (false? (email-address? "hel lo@world.com")))
    (is (false? (email-address? "hello@\nworld.com")))
    (is (false? (email-address? "hello@world.com\r"))))

  (testing "The string adheres to an email address pattern."
    (is (true? (email-address? "hello@world.com")))
    (is (true? (email-address? "hello.world@hello.world.com")))))
