(ns flow.query.current-user-test
  (:require [flow.core :refer :all]
            [flow.entity.authorisation :as authorisation]
            [flow.entity.user :as user]
            [flow.helpers :as h]
            [clojure.test :refer :all]))


(defn setup
  []
  (h/create-test-user! "success+1@simulator.amazonses.com"))

(defn tear-down
  []
  (h/destroy-test-user! "success+1@simulator.amazonses.com"))

(defn fixture [test]
  (tear-down)
  (setup)
  (test)
  (tear-down))

(use-fixtures :each fixture)


(deftest test-current-user

  (testing "The handler negotiates the current-user query when no session is provided."
    (let [request (h/request {:query {:current-user {}}})
          {:keys [status headers body] :as response} (handler request)]
      (is (= 200 status))
      (is (= {:users {}
              :authorisations {}
              :metadata {}
              :session {:current-user-id nil}}
             (h/decode :transit body)))))

  (testing "The handler negotiates the current-user query when an unauthorised session is provided."
    (let [request (h/request
                   {:session :unauthorised
                    :query {:current-user {}}})
          {:keys [status headers body] :as response} (handler request)]
      (is (= 200 status))
      (is (= {:users {}
              :authorisations {}
              :metadata {}
              :session {:current-user-id nil}}
             (h/decode :transit body)))))

  (testing "The handler negotiates the current-user query when an authorised session is provided."
    (let [request (h/request
                   {:session "success+1@simulator.amazonses.com"
                    :query {:current-user {}}})
          user-id (user/id "success+1@simulator.amazonses.com")
          user (user/fetch user-id)
          {:keys [status headers body] :as response} (handler request)]
      (is (= 200 status))
      (is (= {:users {user-id user}
              :authorisations {}
              :metadata {}
              :session {:current-user-id user-id}}
             (h/decode :transit body))))))
