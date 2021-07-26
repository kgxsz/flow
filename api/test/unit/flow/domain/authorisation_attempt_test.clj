(ns flow.domain.authorisation-attempt-test
  (:require [flow.domain.authorisation-attempt :refer :all]
            [flow.specifications :as s]
            [flow.dummy :as d]
            [clojure.test :refer :all]
            [clj-time.coerce :as t.coerce]
            [clj-time.core :as t]))


(def authorisation (first d/authorisations))


(deftest test-grant

  (testing "Returns the authorisation with the granted-at field updated from
            nil to an instant representing now."
    (let [now (t/date-time 2021 4 20)
          authorisation' (assoc authorisation :authorisation/granted-at (t.coerce/to-date now))]
      (with-redefs [t/now (constantly now)]
        (is (= authorisation' (grant authorisation))))))

  (testing "Returns the authorisation unchanged if the authorisation was
            previously granted."
    (let [now (t/date-time 2021 4 20)
          before #inst "2021-04-10T00:00:00.000-00:00"
          authorisation' (assoc authorisation :authorisation/granted-at before)]
      (with-redefs [t/now (constantly now)]
        (is (= authorisation' (grant authorisation')))))))


(deftest test-grantable?

  (testing "Returns false when the authorisation was previously granted."
    (let [now (t/date-time 2021 4 20)
          before #inst "2021-04-10T00:00:00.000-00:00"
          authorisation' (assoc authorisation :authorisation/granted-at before)]
      (with-redefs [t/now (constantly now)]
        (is (false? (grantable? authorisation'))))))

  (testing "Returns false when the authorisation was created more than
            or equal to five minutes ago."
    (let [now (t/date-time 2021 4 20)
          before #inst "2021-04-19T23:55:00.000-00:00"
          authorisation' (assoc authorisation :authorisation/created-at before)]
      (with-redefs [t/now (constantly now)]
        (is (false? (grantable? authorisation')))))
    (let [now (t/date-time 2021 4 20)
          before #inst "2021-04-19T23:54:59.999-00:00"
          authorisation' (assoc authorisation :authorisation/created-at before)]
      (with-redefs [t/now (constantly now)]
        (is (false? (grantable? authorisation'))))))

  (testing "Returns true when the authorisation was not previously granted,
            and was created less than five minutes ago."
    (let [now (t/date-time 2021 4 20)
          before #inst "2021-04-19T23:55:00.001-00:00"
          authorisation' (assoc authorisation :authorisation/created-at before)]
      (with-redefs [t/now (constantly now)]
        (is (true? (grantable? authorisation')))))))


(deftest test-generate-phrase

  (testing "Returns a phrase with three hyphenated random words."
    (is (= 3 (->> (generate-phrase) (re-seq #"[a-z]+") (count))))))


(deftest test-email

  (testing "Returns an email with a relevant subject line."
    (is (= "Complete your sign in"
           (:subject (email "hello-there-world")))))

  (testing "Returns an email with body html containing the provided phrase."
    (is (= "hello-there-world"
           (->> (email "hello-there-world")
                (:body)
                (:html)
                (re-find #"hello-there-world")))))

  (testing "Returns an email with body text containing the provided phrase."
    (is (= "hello-there-world"
           (->> (email "hello-there-world")
                (:body)
                (:text)
                (re-find #"hello-there-world"))))))
