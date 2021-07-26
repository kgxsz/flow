(ns flow.command.finalise-authorisation-attempt-test
  (:require [flow.core :refer :all]
            [flow.entity.authorisation :as authorisation]
            [flow.entity.user :as user]
            [flow.helpers :as h]
            [clojure.test :refer :all]))


(defn setup
  []
  (h/create-test-user! "success+2@simulator.amazonses.com")
  (h/create-test-user! "success+3@simulator.amazonses.com")
  (h/create-test-user! "success+4@simulator.amazonses.com")
  (h/create-test-authorisation! (user/id "success+2@simulator.amazonses.com") "some-phrase")
  (h/create-test-authorisation! (user/id "success+3@simulator.amazonses.com") "some-phrase")
  (h/create-test-authorisation! (user/id "success+4@simulator.amazonses.com") "some-phrase")
  (h/grant-test-authorisation! (user/id "success+2@simulator.amazonses.com") "some-phrase")
  (h/age-test-authorisation! (user/id "success+3@simulator.amazonses.com") "some-phrase"))


(defn tear-down
  []
  (h/destroy-test-user! "success+1@simulator.amazonses.com")
  (h/destroy-test-user! "success+2@simulator.amazonses.com")
  (h/destroy-test-user! "success+3@simulator.amazonses.com")
  (h/destroy-test-user! "success+4@simulator.amazonses.com")
  (h/destroy-test-authorisations! (user/id "success+1@simulator.amazonses.com"))
  (h/destroy-test-authorisations! (user/id "success+2@simulator.amazonses.com"))
  (h/destroy-test-authorisations! (user/id "success+3@simulator.amazonses.com"))
  (h/destroy-test-authorisations! (user/id "success+4@simulator.amazonses.com")))

(defn fixture [test]
  (tear-down)
  (setup)
  (test)
  (tear-down))

(use-fixtures :each fixture)


(deftest test-finalise-authorisation-attempt

  (testing "The handler negotiates the finalise-authorisation-attempt command when the
            command is being made for a non-existent user with no authorisation."
    (let [request {:request-method :post
                   :uri "/"
                   :headers {"content-type" "application/transit+json"
                             "accept" "application/transit+json"
                             "cookie" (h/cookie)}
                   :body (h/encode
                          :transit
                          {:command {:finalise-authorisation-attempt
                                     {:user/email-address "success+1@simulator.amazonses.com"
                                      :authorisation/phrase "hello-world"}}
                           :query {}
                           :metadata {}
                           :session {}})}
          user-id (user/id "success+1@simulator.amazonses.com")
          authorisation-id (authorisation/id user-id "hello-world")
          user (user/fetch user-id)
          authorisation (authorisation/fetch authorisation-id)
          {:keys [status headers body] :as response} (handler request)
          user' (user/fetch user-id)
          authorisation' (authorisation/fetch authorisation-id)]
      (is (= 200 status))
      (is (= {:users {}
              :authorisations {}
              :metadata {}
              :session {:current-user-id nil}}
             (h/decode :transit body)))
      (is (= nil user user'))
      (is (= nil authorisation authorisation'))))

  (testing "The handler negotiates the finalise-authorisation-attempt command when the
            command is being made for a non-existent authorisation."
    (let [request {:request-method :post
                   :uri "/"
                   :headers {"content-type" "application/transit+json"
                             "accept" "application/transit+json"
                             "cookie" (h/cookie)}
                   :body (h/encode
                          :transit
                          {:command {:finalise-authorisation-attempt
                                     {:user/email-address "success+2@simulator.amazonses.com"
                                      :authorisation/phrase "hello-world"}}
                           :query {}
                           :metadata {}
                           :session {}})}
          user-id (user/id "success+2@simulator.amazonses.com")
          authorisation-id (authorisation/id user-id "hello-world")
          user (user/fetch user-id)
          authorisation (authorisation/fetch authorisation-id)
          {:keys [status headers body] :as response} (handler request)
          user' (user/fetch user-id)
          authorisation' (authorisation/fetch authorisation-id)]
      (is (= 200 status))
      (is (= {:users {}
              :authorisations {}
              :metadata {}
              :session {:current-user-id nil}}
             (h/decode :transit body)))
      (is (= user user'))
      (is (= nil authorisation authorisation'))))

  (testing "The handler negotiates the finalise-authorisation-attempt command when the command
            is being made for an existing authorisation that has already been granted."
    (let [request {:request-method :post
                   :uri "/"
                   :headers {"content-type" "application/transit+json"
                             "accept" "application/transit+json"
                             "cookie" (h/cookie)}
                   :body (h/encode
                          :transit
                          {:command {:finalise-authorisation-attempt
                                     {:user/email-address "success+2@simulator.amazonses.com"
                                      :authorisation/phrase "some-phrase"}}
                           :query {}
                           :metadata {}
                           :session {}})}
          user-id (user/id "success+2@simulator.amazonses.com")
          authorisation-id (user/id "success+2@simulator.amazonses.com")
          user (user/fetch user-id)
          authorisation (authorisation/fetch authorisation-id)
          {:keys [status headers body] :as response} (handler request)
          user' (user/fetch user-id)
          authorisation' (authorisation/fetch authorisation-id)]
      (is (= 200 status))
      (is (= {:users {}
              :authorisations {}
              :metadata {}
              :session {:current-user-id nil}}
             (h/decode :transit body)))
      (is (= user user'))
      (is (= authorisation authorisation'))))

  (testing "The handler negotiates the finalise-authorisation-attempt command when the command
            is being made for an existing authorisation that was created 5 minutes ago."
    (let [request {:request-method :post
                   :uri "/"
                   :headers {"content-type" "application/transit+json"
                             "accept" "application/transit+json"
                             "cookie" (h/cookie)}
                   :body (h/encode
                          :transit
                          {:command {:finalise-authorisation-attempt
                                     {:user/email-address "success+3@simulator.amazonses.com"
                                      :authorisation/phrase "some-phrase"}}
                           :query {}
                           :metadata {}
                           :session {}})}
          user-id (user/id "success+3@simulator.amazonses.com")
          authorisation-id (authorisation/id user-id "some-phrase")
          user' (user/fetch user-id)
          authorisation (authorisation/fetch authorisation-id)
          {:keys [status headers body] :as response} (handler request)
          user (user/fetch user-id)
          authorisation' (authorisation/fetch authorisation-id)]
      (is (= 200 status))
      (is (= {:users {}
              :authorisations {}
              :metadata {}
              :session {:current-user-id nil}}
             (h/decode :transit body)))
      (is (= user user'))
      (is (= authorisation authorisation'))))

  (testing "The handler negotiates the finalise-authorisation-attempt command when the
            command is being made for an existing grantable authorisation."
    (let [request {:request-method :post
                   :uri "/"
                   :headers {"content-type" "application/transit+json"
                             "accept" "application/transit+json"
                             "cookie" (h/cookie)}
                   :body (h/encode
                          :transit
                          {:command {:finalise-authorisation-attempt
                                     {:user/email-address "success+4@simulator.amazonses.com"
                                      :authorisation/phrase "some-phrase"}}
                           :query {}
                           :metadata {}
                           :session {}})}
          user-id (user/id "success+4@simulator.amazonses.com")
          authorisation-id (authorisation/id user-id "some-phrase")
          user (user/fetch user-id)
          authorisation (authorisation/fetch authorisation-id)
          {:keys [status headers body] :as response} (handler request)
          user' (user/fetch user-id)
          authorisation' (authorisation/fetch authorisation-id)]
      (is (= 200 status))
      (is (= {:users {}
              :authorisations {}
              :metadata {}
              :session {:current-user-id user-id}}
             (h/decode :transit body)))
      (is (= user user'))
      (is (nil? (:authorisation/granted-at authorisation)))
      (is (some? (:authorisation/granted-at authorisation'))))))
