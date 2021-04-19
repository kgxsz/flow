(ns flow.domain.authorisation-attempt-test
  (:require [flow.domain.authorisation-attempt :refer :all]
            [flow.specifications :as s]
            [clojure.test :refer :all]
            [clj-time.coerce :as t.coerce]
            [clj-time.core :as t]))


(def authorisation {:authorisation/id #uuid "31f3c785-0f5f-530b-841d-7761400e6793"
                    :user/id #uuid "19f3c785-cf5f-530b-841d-6161400e6793"
                    :authorisation/phrase "amount-addition-harbor",
                    :authorisation/created-at #inst "2021-04-03T11:21:46.894-00:00",
                    :authorisation/granted-at nil})


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
