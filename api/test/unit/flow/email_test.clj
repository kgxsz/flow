(ns flow.email-test
  (:require [flow.email :refer :all]
            [flow.specifications :as s]
            [ses-mailer.core :as mailer]
            [flow.dummy :as d]
            [clojure.test :refer :all]))


(def email-address (:user/email-address (first d/users)))

(def email {:subject "Hello world"
            :body {:html "<div>hello world</div>"
                   :text "Hello world"}})


(deftest test-send-email!

  (testing "Throws an exception when the email address violates specification."
    (with-redefs [mailer/send-email (constantly nil)]
      (is (thrown+? [:type :flow/internal-error]
                    (send-email! "" email)))))

  (testing "Throws an exception when the subject violates specification."
    (let [email' (assoc email :subject (->> (repeat 51 "a") (apply str)))]
      (with-redefs [mailer/send-email (constantly nil)]
        (is (thrown+? [:type :flow/internal-error]
                      (send-email! (send-email! email-address email')))))))

  (testing "Throws an exception when the html violates specification."
    (let [email' (assoc-in email [:body :html] (->> (repeat 1001 "a") (apply str)))]
      (with-redefs [mailer/send-email (constantly nil)]
        (is (thrown+? [:type :flow/internal-error]
                      (send-email! email-address email'))))))

  (testing "Throws an exception when the text violates specification."
    (let [email' (assoc-in email [:body :text] (->> (repeat 1001 "a") (apply str)))]
      (with-redefs [mailer/send-email (constantly nil)]
        (is (thrown+? [:type :flow/internal-error]
                      (send-email! email-address email'))))))

  (testing "Throws an exception when an exception is raised by the library."
    (with-redefs [mailer/send-email (fn [_] (throw (Exception. "hello world")))]
      (is (thrown+? [:type :flow/external-error]
                    (send-email! email-address email)))))

  (testing "Returns nil when the operation is executed successfully."
    (with-redefs [mailer/send-email (constantly nil)]
      (is (nil? (send-email! email-address email))))))
