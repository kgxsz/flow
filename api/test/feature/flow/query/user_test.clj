(ns flow.query.user-test
  (:require [flow.core :refer :all]
            [flow.entity.authorisation :as authorisation]
            [flow.entity.user :as user]
            [flow.helpers :as h]
            [clojure.test :refer :all]))

(def accessible-keys
  {#{:customer}
   [:user/id
    :user/name
    :user/created-at
    :user/deleted-at]
   #{:customer :admin}
   [:user/id
    :user/email-address
    :user/name
    :user/roles
    :user/created-at
    :user/deleted-at]
   #{:owner :customer}
   [:user/id
    :user/email-address
    :user/name
    :user/roles
    :user/created-at
    :user/deleted-at]
   #{:owner :customer :admin}
   [:user/id
    :user/email-address
    :user/name
    :user/roles
    :user/created-at
    :user/deleted-at]})

(defn setup
  []
  (h/create-test-user! "success+1@simulator.amazonses.com")
  (h/create-test-user! "success+3@simulator.amazonses.com")
  (h/create-test-user! "success+4@simulator.amazonses.com" "Test" #{:customer :admin}) )

(defn fixture [test]
  (h/ensure-empty-table)
  (setup)
  (test)
  (h/ensure-empty-table))

(use-fixtures :each fixture)

(deftest test-user

  (testing "The handler negotiates the user query when no session is provided."
    (let [request (h/request {:query {:user {:user/id (user/id "success+1@simulator.amazonses.com")}}})
          {:keys [status headers body] :as response} (handler request)]
      (is (= 200 status))
      (is (= {:users {}
              :authorisations {}
              :metadata {}
              :session {:current-user-id nil}}
             (h/decode :transit body)))))

  (testing "The handler negotiates the user query when an unauthorised session is provided."
    (let [request (h/request
                   {:session :unauthorised
                    :query {:user {:user/id (user/id "success+1@simulator.amazonses.com")}}})
          {:keys [status headers body] :as response} (handler request)]
      (is (= 200 status))
      (is (= {:users {}
              :authorisations {}
              :metadata {}
              :session {:current-user-id nil}}
             (h/decode :transit body)))))

  (testing "The handler negotiates the user query when the query is being made for a non-existent
            user and an authorised session is provided."
    (let [request (h/request
                   {:session "success+1@simulator.amazonses.com"
                    :query {:user {:user/id (user/id "success+2@simulator.amazonses.com")}}})
          {:keys [status headers body] :as response} (handler request)]
      (is (= 200 status))
      (is (= {:users {}
              :authorisations {}
              :metadata {}
              :session {:current-user-id (user/id "success+1@simulator.amazonses.com")}}
             (h/decode :transit body)))))

  (testing "The handler negotiates the user query when the query is being made for an existing
            user and an authorised session for a user with a customer role is provided."
    (let [request (h/request
                   {:session "success+3@simulator.amazonses.com"
                    :query {:user {:user/id (user/id "success+1@simulator.amazonses.com")}}})
          {:keys [status headers body] :as response} (handler request)]
      (is (= 200 status))
      (is (= {:users {(user/id "success+1@simulator.amazonses.com")
                      (-> (user/id "success+1@simulator.amazonses.com")
                          (user/fetch)
                          (select-keys (get accessible-keys #{:customer})))}
              :authorisations {}
              :metadata {}
              :session {:current-user-id (user/id "success+3@simulator.amazonses.com")}}
             (h/decode :transit body)))))

  (testing "The handler negotiates the user query when the query is being made for an existing
            user and an authorised session for a user with a customer role is provided, where
            the authorised user happens to be the same user being queried."
    (let [request (h/request
                   {:session "success+3@simulator.amazonses.com"
                    :query {:user {:user/id (user/id "success+3@simulator.amazonses.com")}}})
          {:keys [status headers body] :as response} (handler request)]
      (is (= 200 status))
      (is (= {:users {(user/id "success+3@simulator.amazonses.com")
                      (-> (user/id "success+3@simulator.amazonses.com")
                          (user/fetch)
                          (select-keys (get accessible-keys #{:owner :customer})))}
              :authorisations {}
              :metadata {}
              :session {:current-user-id (user/id "success+3@simulator.amazonses.com")}}
             (h/decode :transit body)))))

  (testing "The handler negotiates the user query when the query is being made for an existing user
            and an authorised session for a user with both a customer and admin role is provided."
    (let [request (h/request
                   {:session "success+4@simulator.amazonses.com"
                    :query {:user {:user/id (user/id "success+1@simulator.amazonses.com")}}})
          {:keys [status headers body] :as response} (handler request)]
      (is (= 200 status))
      (is (= {:users {(user/id "success+1@simulator.amazonses.com")
                      (-> (user/id "success+1@simulator.amazonses.com")
                          (user/fetch)
                          (select-keys (get accessible-keys #{:customer :admin})))}
              :authorisations {}
              :metadata {}
              :session {:current-user-id (user/id "success+4@simulator.amazonses.com")}}
             (h/decode :transit body)))))

  (testing "The handler negotiates the user query when the query is being made for an existing user
            and an authorised session for a user with both a customer and admin role is provided,
            where the authorised user happens to be the same user being queried."
    (let [request (h/request
                   {:session "success+4@simulator.amazonses.com"
                    :query {:user {:user/id (user/id "success+4@simulator.amazonses.com")}}})
          {:keys [status headers body] :as response} (handler request)]
      (is (= 200 status))
      (is (= {:users {(user/id "success+4@simulator.amazonses.com")
                      (-> (user/id "success+4@simulator.amazonses.com")
                          (user/fetch)
                          (select-keys (get accessible-keys #{:owner :customer :admin})))}
              :authorisations {}
              :metadata {}
              :session {:current-user-id (user/id "success+4@simulator.amazonses.com")}}
             (h/decode :transit body))))))
