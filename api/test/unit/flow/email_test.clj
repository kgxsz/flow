(ns flow.email-test
  (:require [flow.email :refer :all]
            [ses-mailer.core :as mailer]
            [clojure.test :refer :all]))


(def email-address "j.mcjohnson@gmail.com")
(def subject "Hello world")
(def body {:html "<div>hello world</div>"
           :text "Hello world"})


(deftest test-send-email!

  (testing "The email address violates specification."
    (with-redefs [mailer/send-email (constantly nil)]
      (is (thrown? IllegalStateException (send-email!
                                          ""
                                          subject
                                          body)))))

  (testing "The subject violates specification."
    (with-redefs [mailer/send-email (constantly nil)]
      (is (thrown? IllegalStateException
                   (send-email! (send-email!
                                 email-address
                                 (->> (repeat 251 "a") (apply str))
                                 body))))))

  (testing "The body violates specification."
    (with-redefs [mailer/send-email (constantly nil)]
      (is (thrown? IllegalStateException
                   (send-email! email-address
                                subject
                                (assoc body :html (->> (repeat 10001 "a") (apply str))))))
      (is (thrown? IllegalStateException
                   (send-email! email-address
                                subject
                                (assoc body :text (->> (repeat 10001 "a") (apply str))))))))

  (testing "No specifications violated."
    (with-redefs [mailer/send-email (constantly nil)]
      (is (nil? (send-email! email-address subject body))))))
