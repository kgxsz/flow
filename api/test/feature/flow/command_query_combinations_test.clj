(ns flow.command-query-combinations-test
  (:require [flow.core :refer :all]
            [flow.entity.authorisation :as authorisation]
            [flow.entity.user :as user]
            [flow.helpers :as h]
            [clojure.test :refer :all]))

(defn setup
  []
  (h/create-test-user! "success+1@simulator.amazonses.com" "Test" #{:customer :admin})
  (h/create-test-user! "success+2@simulator.amazonses.com")
  (h/create-test-user! "success+3@simulator.amazonses.com")
  (h/create-test-user! "success+5@simulator.amazonses.com")
  (h/create-test-authorisation! (user/id "success+2@simulator.amazonses.com") "some-phrase")
  (h/create-test-authorisation! (user/id "success+3@simulator.amazonses.com") "some-phrase"))

(defn fixture [test]
  (h/ensure-empty-table)
  (setup)
  (test)
  (h/ensure-empty-table))

(use-fixtures :each fixture)

(deftest test-command-query-combinations

  (testing "The handler negotiates multiple queries at once."
    (let [request (h/request
                   {:session "success+1@simulator.amazonses.com"
                    :query {:user {:user/id (user/id "success+2@simulator.amazonses.com")}
                            :current-user {}
                            :authorisations {}}})
          {:keys [status headers body] :as response} (handler request)]
      (is (= 200 status))
      (is (= {:users {(user/id "success+1@simulator.amazonses.com")
                      (-> (user/id "success+1@simulator.amazonses.com")
                          (user/fetch)
                          (select-keys (get-in h/accessible-keys [:user #{:owner :admin :customer}])))
                      (user/id "success+2@simulator.amazonses.com")
                      (-> (user/id "success+2@simulator.amazonses.com")
                          (user/fetch)
                          (select-keys (get-in h/accessible-keys [:user #{:admin :customer}])))}
              :authorisations {(authorisation/id (user/id "success+2@simulator.amazonses.com") "some-phrase")
                               (-> (user/id "success+2@simulator.amazonses.com")
                                   (authorisation/id "some-phrase")
                                   (authorisation/fetch)
                                   (select-keys (get-in h/accessible-keys [:authorisation #{:customer :admin}])))
                               (authorisation/id (user/id "success+3@simulator.amazonses.com") "some-phrase")
                               (-> (user/id "success+3@simulator.amazonses.com")
                                   (authorisation/id "some-phrase")
                                   (authorisation/fetch)
                                   (select-keys (get-in h/accessible-keys [:authorisation #{:customer :admin}])))}
              :metadata {}
              :session {:current-user-id (user/id "success+1@simulator.amazonses.com")}}
             (h/decode :transit body)))))

  (testing "The handler negotiates multiple commands at once."
    (let [request (h/request
                   {:session "success+1@simulator.amazonses.com"
                    :command {:add-user
                              {:user/id #uuid "00000000-0000-0000-0000-000000000000"
                               :user/email-address "success+4@simulator.amazonses.com"
                               :user/name "Test"
                               :user/roles #{:customer}}
                              :delete-user
                              {:user/id (user/id "success+5@simulator.amazonses.com")}}})
          user-a (user/fetch (user/id "success+4@simulator.amazonses.com"))
          user-b (user/fetch (user/id "success+5@simulator.amazonses.com"))
          {:keys [status headers body] :as response} (handler request)
          user-a' (user/fetch (user/id "success+4@simulator.amazonses.com"))
          user-b' (user/fetch (user/id "success+5@simulator.amazonses.com"))]
      (is (= 200 status))
      (is (= {:users {}
              :authorisations {}
              :metadata {:id-resolution
                         {#uuid "00000000-0000-0000-0000-000000000000"
                          (user/id "success+4@simulator.amazonses.com")}}
              :session {:current-user-id (user/id "success+1@simulator.amazonses.com")}}
             (h/decode :transit body)))
      (is (nil? user-a))
      (is (some? user-a'))
      (is (nil? (:user/deleted-at user-b)))
      (is (some? (:user/deleted-at user-b'))))))
