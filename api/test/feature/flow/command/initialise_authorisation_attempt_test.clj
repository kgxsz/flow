(ns flow.command.initialise-authorisation-attempt-test
  (:require [flow.core :refer :all]
            [flow.entity.authorisation :as authorisation]
            [flow.entity.user :as user]
            [flow.domain.user-management :as user-management]
            [flow.helpers :as h]
            [clojure.test :refer :all]))


(defn setup
  []
  (h/create-test-user! "success+2@simulator.amazonses.com")
  (h/delete-test-user! "success+2@simulator.amazonses.com")
  (h/create-test-user! "success+3@simulator.amazonses.com")
  (h/create-test-user! "success+4@simulator.amazonses.com")
  (h/create-test-user! "success+5@simulator.amazonses.com")
  (h/create-test-user! "success+6@simulator.amazonses.com"))

(defn fixture [test]
  (h/ensure-empty-table)
  (setup)
  (test)
  (h/ensure-empty-table))

(use-fixtures :each fixture)


(deftest test-initialise-authorisation-attempt

  (testing "The handler negotiates the initialise-authorisation-attempt command when the
            command is being made for a non-existent user."
    (let [request (h/request
                   {:session :unauthorised
                    :command {:initialise-authorisation-attempt
                              {:user/email-address "success+1@simulator.amazonses.com"}}})
          user-id (user/id "success+1@simulator.amazonses.com")
          user (user/fetch user-id)
          authorisations (h/find-test-authorisations user-id)
          {:keys [status headers body] :as response} (handler request)
          user' (user/fetch user-id)
          authorisations' (h/find-test-authorisations user-id)]
      (is (= 200 status))
      (is (= {:users {}
              :authorisations {}
              :metadata {}
              :session {:current-user-id nil}}
             (h/decode :transit body)))
      (is (= nil user user'))
      (is (empty? authorisations))
      (is (empty? authorisations'))))

  (testing "The handler negotiates the initialise-authorisation-attempt command when the
            command is being made for an existing user who has prevously been deleted."
    (let [request (h/request
                   {:session :unauthorised
                    :command {:initialise-authorisation-attempt
                              {:user/email-address "success+2@simulator.amazonses.com"}}})
          user-id (user/id "success+2@simulator.amazonses.com")
          user (user/fetch user-id)
          authorisations (h/find-test-authorisations user-id)
          {:keys [status headers body] :as response} (handler request)
          user' (user/fetch user-id)
          authorisations' (h/find-test-authorisations user-id)]
      (is (= 200 status))
      (is (= {:users {}
              :authorisations {}
              :metadata {}
              :session {:current-user-id nil}}
             (h/decode :transit body)))
      (is (= user user'))
      (is (empty? authorisations))
      (is (empty? authorisations'))))

  (testing "The handler negotiates the initialise-authorisation-attempt command when the command
            is being made for an existing user and no session is provided."
    (let [request (h/request
                   {:command {:initialise-authorisation-attempt
                              {:user/email-address "success+3@simulator.amazonses.com"}}})
          user-id (user/id "success+3@simulator.amazonses.com")
          user (user/fetch user-id)
          authorisations (h/find-test-authorisations user-id)
          {:keys [status headers body] :as response} (handler request)
          user' (user/fetch user-id)
          authorisations' (h/find-test-authorisations user-id)]
      (is (= 200 status))
      (is (= {:users {}
              :authorisations {}
              :metadata {}
              :session {:current-user-id nil}}
             (h/decode :transit body)))
      (is (= user user'))
      (is (= 0 (count authorisations)))
      (is (= 1 (count authorisations')))))

  (testing "The handler negotiates the initialise-authorisation-attempt command when the command
            is being made for an existing user and an unauthorised session is provided."
    (let [request (h/request
                   {:session :unauthorised
                    :command {:initialise-authorisation-attempt
                              {:user/email-address "success+4@simulator.amazonses.com"}}})
          user-id (user/id "success+4@simulator.amazonses.com")
          user (user/fetch user-id)
          authorisations (h/find-test-authorisations user-id)
          {:keys [status headers body] :as response} (handler request)
          user' (user/fetch user-id)
          authorisations' (h/find-test-authorisations user-id)]
      (is (= 200 status))
      (is (= {:users {}
              :authorisations {}
              :metadata {}
              :session {:current-user-id nil}}
             (h/decode :transit body)))
      (is (= user user'))
      (is (= 0 (count authorisations)))
      (is (= 1 (count authorisations')))))

  (testing "The handler negotiates the initialise-authorisation-attempt command when the command
            is being made for an existing user and a session authorised to that user is provided."
    (let [request (h/request
                   {:session "success+5@simulator.amazonses.com"
                    :command {:initialise-authorisation-attempt
                              {:user/email-address "success+5@simulator.amazonses.com"}}})
          user-id (user/id "success+5@simulator.amazonses.com")
          user (user/fetch user-id)
          authorisations (h/find-test-authorisations user-id)
          {:keys [status headers body] :as response} (handler request)
          user' (user/fetch user-id)
          authorisations' (h/find-test-authorisations user-id)]
      (is (= 200 status))
      (is (= {:users {}
              :authorisations {}
              :metadata {}
              :session {:current-user-id user-id}}
             (h/decode :transit body)))
      (is (= user user'))
      (is (= 0 (count authorisations)))
      (is (= 1 (count authorisations')))))

  (testing "The handler negotiates the initialise-authorisation-attempt command when the command is being
            made for an existing user and a session authorised to a different user is provided."
    (let [request (h/request
                   {:session "success+5@simulator.amazonses.com"
                    :command {:initialise-authorisation-attempt
                              {:user/email-address "success+6@simulator.amazonses.com"}}})
          user-id (user/id "success+6@simulator.amazonses.com")
          user (user/fetch user-id)
          authorisations (h/find-test-authorisations user-id)
          {:keys [status headers body] :as response} (handler request)
          user' (user/fetch user-id)
          authorisations' (h/find-test-authorisations user-id)]
      (is (= 200 status))
      (is (= {:users {}
              :authorisations {}
              :metadata {}
              :session {:current-user-id (user/id "success+5@simulator.amazonses.com")}}
             (h/decode :transit body)))
      (is (= user user'))
      (is (= 0 (count authorisations)))
      (is (= 1 (count authorisations'))))) )
