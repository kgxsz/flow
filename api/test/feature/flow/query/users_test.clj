(ns flow.query.users-test
  (:require [flow.core :refer :all]
            [flow.entity.authorisation :as authorisation]
            [flow.entity.user :as user]
            [flow.helpers :as h]
            [clojure.test :refer :all]))


(defn setup
  []
  (h/create-test-user! "success+1@simulator.amazonses.com"))

(defn fixture [test]
  (h/ensure-empty-table)
  (setup)
  (test)
  (h/ensure-empty-table))

(use-fixtures :each fixture)


(deftest test-users

  (testing "The handler negotiates the current-user query when no session is provided."
    (let [request (h/request {:query {:users {}}})
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
                    :query {:users {}}})
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
                    :query {:users {}}})
          user-id (user/id "success+1@simulator.amazonses.com")
          user (user/fetch user-id)
          users (user/fetch-all)
          {:keys [status headers body] :as response} (handler request)]
      (is (= 200 status))
      (is (= {:users {user-id user}
              :authorisations {}
              :metadata {}
              :session {:current-user-id user-id}}
             (h/decode :transit body))))))
