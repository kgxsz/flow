(ns flow.session-test
  (:require [flow.core :refer :all]
            [flow.entity.user :as user]
            [flow.helpers :as h]
            [clojure.test :refer :all]))


(defn setup
  []
  (h/create-test-user! "success+2@simulator.amazonses.com"))

(defn fixture [test]
  (h/ensure-empty-table)
  (setup)
  (test)
  (h/ensure-empty-table))

(use-fixtures :each fixture)


(deftest test-session

  (testing "The handler returns a session cookie and corresponding session when no cookie is provided."
    (let [request (h/request)
          {:keys [status headers body] :as response} (handler request)]
      (is (some? (get headers "Set-Cookie")))
      (is (= {:current-user-id nil} (:session (h/decode :transit body))))))

  (testing "The handler returns a session cookie and corresponding session when a cookie
            containing an unauthorised session is provided."
    (let [request (h/request {:session :unauthorised})
          {:keys [status headers body] :as response} (handler request)]
      (is (some? (get headers "Set-Cookie")))
      (is (= {:current-user-id nil} (:session (h/decode :transit body))))))

  (testing "The handler returns an internal error when a cookie containing an authorised session
            for a non-existent user is provided."
    (let [request (h/request {:session "success+1@simulator.amazonses.com"})
          {:keys [status headers body] :as response} (handler request)]
      (is (= 500 status))))

  (testing "The handler returns a session cookie and corresponding session when a cookie
            containing an authorised session for an existing user is provided."
    (let [request (h/request {:session "success+2@simulator.amazonses.com"})
          {:keys [status headers body] :as response} (handler request)
          {:keys [session]} (h/decode :transit body)]
      (is (some? (get headers "Set-Cookie")))
      (is (= {:current-user-id (user/id "success+2@simulator.amazonses.com")} session)))))
