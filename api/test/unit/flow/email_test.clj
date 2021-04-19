(ns flow.email-test
  (:require [flow.email :refer :all]
            [flow.specifications :as s]
            [ses-mailer.core :as mailer]
            [clojure.test :refer :all]))


(def email-address "j.mcjohnson@gmail.com")
(def subject "Hello world")
(def body {:html "<div>hello world</div>"
           :text "Hello world"})


(deftest test-send-email!

  (testing "Throws an exception when the email address violates specification."
    (with-redefs [mailer/send-email (constantly nil)]
      (is (thrown? IllegalStateException (send-email!
                                          ""
                                          subject
                                          body)))))

  (testing "Throws an exception when the subject violates specification."
    (with-redefs [mailer/send-email (constantly nil)]
      (is (thrown? IllegalStateException
                   (send-email! (send-email!
                                 email-address
                                 (->> (repeat 51 "a") (apply str))
                                 body))))))

  (testing "Throws an exception when the body violates specification."
    (with-redefs [mailer/send-email (constantly nil)]
      (is (thrown? IllegalStateException
                   (send-email! email-address
                                subject
                                (assoc body :html (->> (repeat 1001 "a") (apply str))))))
      (is (thrown? IllegalStateException
                   (send-email! email-address
                                subject
                                (assoc body :text (->> (repeat 1001 "a") (apply str))))))))

  (testing "Returns nil when the operation is executed successfully."
    (with-redefs [mailer/send-email (constantly nil)]
      (is (nil? (send-email! email-address subject body))))))
