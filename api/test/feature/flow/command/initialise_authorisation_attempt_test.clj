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
  (h/create-test-user! "success+3@simulator.amazonses.com")
  (h/create-test-user! "success+4@simulator.amazonses.com")
  (h/create-test-user! "success+5@simulator.amazonses.com")
  (h/delete-test-user! "success+2@simulator.amazonses.com"))

(defn tear-down
  []
  (h/destroy-test-user! "success+1@simulator.amazonses.com")
  (h/destroy-test-user! "success+2@simulator.amazonses.com")
  (h/destroy-test-user! "success+3@simulator.amazonses.com")
  (h/destroy-test-user! "success+4@simulator.amazonses.com")
  (h/destroy-test-user! "success+5@simulator.amazonses.com")
  (h/destroy-test-authorisations! (user/id "success+1@simulator.amazonses.com"))
  (h/destroy-test-authorisations! (user/id "success+2@simulator.amazonses.com"))
  (h/destroy-test-authorisations! (user/id "success+3@simulator.amazonses.com"))
  (h/destroy-test-authorisations! (user/id "success+4@simulator.amazonses.com"))
  (h/destroy-test-authorisations! (user/id "success+5@simulator.amazonses.com")))

(defn fixture [test]
  (tear-down)
  (setup)
  (test)
  (tear-down))

(use-fixtures :each fixture)


(deftest test-initialise-authorisation-attempt

  (testing "The handler negotiates the initialise-authorisation-attempt command when the
            command is being made for a non-existent user."
    (let [request {:request-method :post
                   :uri "/"
                   :headers {"content-type" "application/transit+json"
                             "accept" "application/transit+json"
                             "cookie" (h/cookie)}
                   :body (h/encode
                          :transit
                          {:command {:initialise-authorisation-attempt
                                     {:user/email-address "success+1@simulator.amazonses.com"}}
                           :query {}
                           :metadata {}
                           :session {}})}
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
            command is being made for a deleted user."
    (let [request {:request-method :post
                   :uri "/"
                   :headers {"content-type" "application/transit+json"
                             "accept" "application/transit+json"
                             "cookie" (h/cookie)}
                   :body (h/encode
                          :transit
                          {:command {:initialise-authorisation-attempt
                                     {:user/email-address "success+2@simulator.amazonses.com"}}
                           :query {}
                           :metadata {}
                           :session {}})}
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
            is being made for an existing user and a session authorised to the user is provided."
    (let [request {:request-method :post
                   :uri "/"
                   :headers {"content-type" "application/transit+json"
                             "accept" "application/transit+json"
                             "cookie" (h/cookie "success+4@simulator.amazonses.com")}
                   :body (h/encode
                          :transit
                          {:command {:initialise-authorisation-attempt
                                     {:user/email-address "success+4@simulator.amazonses.com"}}
                           :query {}
                           :metadata {}
                           :session {}})}
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
              :session {:current-user-id user-id}}
             (h/decode :transit body)))
      (is (= user user'))
      (is (= 0 (count authorisations)))
      (is (= 1 (count authorisations')))))

  (testing "The handler negotiates the initialise-authorisation-attempt command when the command is being
            made for an existing user and a session authorised to a different user is provided."
    (let [request {:request-method :post
                   :uri "/"
                   :headers {"content-type" "application/transit+json"
                             "accept" "application/transit+json"
                             "cookie" (h/cookie "success+4@simulator.amazonses.com")}
                   :body (h/encode
                          :transit
                          {:command {:initialise-authorisation-attempt
                                     {:user/email-address "success+5@simulator.amazonses.com"}}
                           :query {}
                           :metadata {}
                           :session {}})}
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
              :session {:current-user-id (user/id "success+4@simulator.amazonses.com")}}
             (h/decode :transit body)))
      (is (= user user'))
      (is (= 0 (count authorisations)))
      (is (= 1 (count authorisations')))))

  (testing "The handler negotiates the initialise-authorisation-attempt command when the command
            is being made for an existing user and an unauthorised session is provided."
    (let [request {:request-method :post
                   :uri "/"
                   :headers {"content-type" "application/transit+json"
                             "accept" "application/transit+json"
                             "cookie" (h/cookie)}
                   :body (h/encode
                          :transit
                          {:command {:initialise-authorisation-attempt
                                     {:user/email-address "success+3@simulator.amazonses.com"}}
                           :query {}
                           :metadata {}
                           :session {}})}
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
      (is (= 1 (count authorisations'))))))
