(ns flow.query.authorisations-test
  (:require [flow.core :refer :all]
            [flow.entity.authorisation :as authorisation]
            [flow.entity.user :as user]
            [flow.helpers :as h]
            [clojure.test :refer :all]))


(defn setup
  []
  (h/create-test-user! "success+1@simulator.amazonses.com")
  (h/create-test-user! "success+2@simulator.amazonses.com" "Test" #{:customer :admin})
  (h/create-test-authorisation! (user/id "success+1@simulator.amazonses.com") "some-phrase")
  (h/create-test-authorisation! (user/id "success+1@simulator.amazonses.com") "some-other-phrase")
  (h/create-test-authorisation! (user/id "success+2@simulator.amazonses.com") "some-phrase"))

(defn fixture [test]
  (h/ensure-empty-table)
  (setup)
  (test)
  (h/ensure-empty-table))

(use-fixtures :each fixture)

(deftest test-authorisations

  (testing "The handler negotiates the authorisations query when no session is provided."
    (let [request (h/request {:query {:authorisations {}}})
          {:keys [status headers body] :as response} (handler request)]
      (is (= 200 status))
      (is (= {:users {}
              :authorisations {}
              :metadata {}
              :session {:current-user-id nil}}
             (h/decode :transit body)))))

  (testing "The handler negotiates the authorisations query when an unauthorised session is provided."
    (let [request (h/request
                   {:session :unauthorised
                    :query {:authorisations {}}})
          {:keys [status headers body] :as response} (handler request)]
      (is (= 200 status))
      (is (= {:users {}
              :authorisations {}
              :metadata {}
              :session {:current-user-id nil}}
             (h/decode :transit body)))))

  (testing "The handler negotiates the authorisations query when an authorised session is provided
            for a user with a customer role."
    (let [request (h/request
                   {:session "success+1@simulator.amazonses.com"
                    :query {:authorisations {}}})
          {:keys [status headers body] :as response} (handler request)]
      (is (= 200 status))
      (is (= {:users {}
              :authorisations {}
              :metadata {}
              :session {:current-user-id (user/id "success+1@simulator.amazonses.com")}}
             (h/decode :transit body)))))

  (testing "The handler negotiates the authorisations query when an authorised session for a user
            with both a customer and admin role is provided."
    (let [request (h/request
                   {:session "success+2@simulator.amazonses.com"
                    :query {:authorisations {}}})
          {:keys [status headers body] :as response} (handler request)]
      (is (= 200 status))
      (is (= {:users {}
              :authorisations {(authorisation/id (user/id "success+1@simulator.amazonses.com") "some-phrase")
                               (-> (user/id "success+1@simulator.amazonses.com")
                                   (authorisation/id "some-phrase")
                                   (authorisation/fetch)
                                   (select-keys (get-in h/accessible-keys [:authorisation #{:customer :admin}])))
                               (authorisation/id (user/id "success+1@simulator.amazonses.com") "some-other-phrase")
                               (-> (user/id "success+1@simulator.amazonses.com")
                                   (authorisation/id "some-other-phrase")
                                   (authorisation/fetch)
                                   (select-keys (get-in h/accessible-keys [:authorisation #{:customer :admin}])))
                               (authorisation/id (user/id "success+2@simulator.amazonses.com") "some-phrase")
                               (-> (user/id "success+2@simulator.amazonses.com")
                                   (authorisation/id "some-phrase")
                                   (authorisation/fetch)
                                   (select-keys (get-in h/accessible-keys [:authorisation #{:owner :customer :admin}])))}
              :metadata {}
              :session {:current-user-id (user/id "success+2@simulator.amazonses.com")}}
             (h/decode :transit body))))))
