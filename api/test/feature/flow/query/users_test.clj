(ns flow.query.users-test
  (:require [flow.core :refer :all]
            [flow.entity.authorisation :as authorisation]
            [flow.entity.user :as user]
            [flow.helpers :as h]
            [clojure.test :refer :all]))


(defn setup
  []
  (h/create-test-user! "success+1@simulator.amazonses.com")
  (h/create-test-user! "success+2@simulator.amazonses.com" "Test" #{:customer :admin})
  (h/create-test-user! "success+3@simulator.amazonses.com"))

(defn fixture [test]
  (h/ensure-empty-table)
  (setup)
  (test)
  (h/ensure-empty-table))

(use-fixtures :each fixture)


(deftest test-users

  (testing "The handler negotiates the users query when no session is provided."
    (let [request (h/request {:query {:users {}}})
          {:keys [status headers body] :as response} (handler request)]
      (is (= 200 status))
      (is (= {:users {}
              :authorisations {}
              :metadata {:users {:next-offset (user/id "success+1@simulator.amazonses.com")}}
              :session {:current-user-id nil}}
             (h/decode :transit body)))))

  (testing "The handler negotiates the users query when an unauthorised session is provided."
    (let [request (h/request
                   {:session :unauthorised
                    :query {:users {}}})
          {:keys [status headers body] :as response} (handler request)]
      (is (= 200 status))
      (is (= {:users {}
              :authorisations {}
              :metadata {:users {:next-offset (user/id "success+1@simulator.amazonses.com")}}
              :session {:current-user-id nil}}
             (h/decode :transit body)))))

  (testing "The handler negotiates the users query when an authorised session is provided
            for a user with a customer role."
    (let [request (h/request
                   {:session "success+1@simulator.amazonses.com"
                    :query {:users {}}})
          {:keys [status headers body] :as response} (handler request)]
      (is (= 200 status))
      (is (= {:users {(user/id "success+1@simulator.amazonses.com")
                      (-> (user/id "success+1@simulator.amazonses.com")
                          (user/fetch)
                          (select-keys (get-in h/accessible-keys [:user #{:owner :customer}])))
                      (user/id "success+2@simulator.amazonses.com")
                      (-> (user/id "success+2@simulator.amazonses.com")
                          (user/fetch)
                          (select-keys (get-in h/accessible-keys [:user #{:customer}])))
                      (user/id "success+3@simulator.amazonses.com")
                      (-> (user/id "success+3@simulator.amazonses.com")
                          (user/fetch)
                          (select-keys (get-in h/accessible-keys [:user #{:customer}])))}
              :authorisations {}
              :metadata {:users {:next-offset (user/id "success+1@simulator.amazonses.com")}}
              :session {:current-user-id (user/id "success+1@simulator.amazonses.com")}}
             (h/decode :transit body)))))

  (testing "The handler negotiates the users query when an authorised session for a user
            with both a customer and admin role is provided."
    (let [request (h/request
                   {:session "success+2@simulator.amazonses.com"
                    :query {:users {}}})
          {:keys [status headers body] :as response} (handler request)]
      (is (= 200 status))
      (is (= {:users {(user/id "success+1@simulator.amazonses.com")
                      (-> (user/id "success+1@simulator.amazonses.com")
                          (user/fetch)
                          (select-keys (get-in h/accessible-keys [:user #{:customer :admin}])))
                      (user/id "success+2@simulator.amazonses.com")
                      (-> (user/id "success+2@simulator.amazonses.com")
                          (user/fetch)
                          (select-keys (get-in h/accessible-keys [:user #{:owner :customer :admin}])))
                      (user/id "success+3@simulator.amazonses.com")
                      (-> (user/id "success+3@simulator.amazonses.com")
                          (user/fetch)
                          (select-keys (get-in h/accessible-keys [:user #{:customer :admin}])))}
              :authorisations {}
              :metadata {:users {:next-offset (user/id "success+1@simulator.amazonses.com")}}
              :session {:current-user-id (user/id "success+2@simulator.amazonses.com")}}
             (h/decode :transit body)))))

(testing "The handler negotiates the users query when a limit is provided."
    (let [request (h/request
                   {:session "success+1@simulator.amazonses.com"
                    :metadata {:users {:limit 2 :offset nil}}
                    :query {:users {}}})
          {:keys [status headers body] :as response} (handler request)]
      (is (= 200 status))
      (is (= {:users {(user/id "success+2@simulator.amazonses.com")
                      (-> (user/id "success+2@simulator.amazonses.com")
                          (user/fetch)
                          (select-keys (get-in h/accessible-keys [:user #{:customer}])))
                      (user/id "success+3@simulator.amazonses.com")
                      (-> (user/id "success+3@simulator.amazonses.com")
                          (user/fetch)
                          (select-keys (get-in h/accessible-keys [:user #{:customer}])))}
              :authorisations {}
              :metadata {:users {:next-offset (user/id "success+2@simulator.amazonses.com")}}
              :session {:current-user-id (user/id "success+1@simulator.amazonses.com")}}
             (h/decode :transit body)))))

(testing "The handler negotiates the users query when an offset is provided."
    (let [request (h/request
                   {:session "success+1@simulator.amazonses.com"
                    :metadata {:users {:limit 2 :offset (user/id "success+2@simulator.amazonses.com")}}
                    :query {:users {}}})
          {:keys [status headers body] :as response} (handler request)]
      (is (= 200 status))
      (is (= {:users {(user/id "success+1@simulator.amazonses.com")
                      (-> (user/id "success+1@simulator.amazonses.com")
                          (user/fetch)
                          (select-keys (get-in h/accessible-keys [:user #{:owner :customer}])))}
              :authorisations {}
              :metadata {:users {:next-offset (user/id "success+1@simulator.amazonses.com")}}
              :session {:current-user-id (user/id "success+1@simulator.amazonses.com")}}
             (h/decode :transit body)))))

  (testing "The handler negotiates the users query when there's no items left."
    (let [request (h/request
                   {:session "success+1@simulator.amazonses.com"
                    :metadata {:users {:limit 2 :offset (user/id "success+1@simulator.amazonses.com")}}
                    :query {:users {}}})
          {:keys [status headers body] :as response} (handler request)]
      (is (= 200 status))
      (is (= {:users {}
              :authorisations {}
              :metadata {:users {:next-offset nil}}
              :session {:current-user-id (user/id "success+1@simulator.amazonses.com")}}
             (h/decode :transit body))))))
