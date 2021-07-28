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
  (h/create-test-user! "success+5@simulator.amazonses.com")
  (h/create-test-user! "success+6@simulator.amazonses.com")
  (h/create-test-user! "success+7@simulator.amazonses.com")
  (h/create-test-authorisation! (user/id "success+2@simulator.amazonses.com") "some-phrase")
  (h/create-test-authorisation! (user/id "success+3@simulator.amazonses.com") "some-phrase")
  (h/create-test-authorisation! (user/id "success+4@simulator.amazonses.com") "some-phrase")
  (h/create-test-authorisation! (user/id "success+5@simulator.amazonses.com") "some-phrase")
  (h/create-test-authorisation! (user/id "success+6@simulator.amazonses.com") "some-phrase")
  (h/create-test-authorisation! (user/id "success+7@simulator.amazonses.com") "some-phrase")
  (h/grant-test-authorisation! (user/id "success+2@simulator.amazonses.com") "some-phrase")
  (h/age-test-authorisation! (user/id "success+3@simulator.amazonses.com") "some-phrase"))


(defn tear-down
  []
  (h/destroy-test-user! "success+1@simulator.amazonses.com")
  (h/destroy-test-user! "success+2@simulator.amazonses.com")
  (h/destroy-test-user! "success+3@simulator.amazonses.com")
  (h/destroy-test-user! "success+4@simulator.amazonses.com")
  (h/destroy-test-user! "success+5@simulator.amazonses.com")
  (h/destroy-test-user! "success+6@simulator.amazonses.com")
  (h/destroy-test-user! "success+7@simulator.amazonses.com")
  (h/destroy-test-authorisations! (user/id "success+1@simulator.amazonses.com"))
  (h/destroy-test-authorisations! (user/id "success+2@simulator.amazonses.com"))
  (h/destroy-test-authorisations! (user/id "success+3@simulator.amazonses.com"))
  (h/destroy-test-authorisations! (user/id "success+4@simulator.amazonses.com"))
  (h/destroy-test-authorisations! (user/id "success+5@simulator.amazonses.com"))
  (h/destroy-test-authorisations! (user/id "success+7@simulator.amazonses.com"))
  (h/destroy-test-authorisations! (user/id "success+6@simulator.amazonses.com")))

(defn fixture [test]
  (tear-down)
  (setup)
  (test)
  (tear-down))

(use-fixtures :each fixture)


(deftest test-finalise-authorisation-attempt

  (testing "The handler negotiates the finalise-authorisation-attempt command when the
            command is being made for a non-existent user and authorisation."
    (let [request (h/request
                   {:command {:finalise-authorisation-attempt
                              {:user/email-address "success+1@simulator.amazonses.com"
                               :authorisation/phrase "hello-world"}}})
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
            command is being made for an existing user with a non-existent authorisation."
    (let [request (h/request
                   {:command {:finalise-authorisation-attempt
                              {:user/email-address "success+2@simulator.amazonses.com"
                               :authorisation/phrase "hello-world"}}})
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

  (testing "The handler negotiates the finalise-authorisation-attempt command when the command is being
            made for an existing user and authorisation where the authorisation has already been granted."
    (let [request (h/request
                   {:command {:finalise-authorisation-attempt
                              {:user/email-address "success+2@simulator.amazonses.com"
                               :authorisation/phrase "some-phrase"}}})
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

  (testing "The handler negotiates the finalise-authorisation-attempt command when the command is being
            made for an existing user and authorisation where the authorisation was created 5 minutes ago."
    (let [request (h/request
                   {:command {:finalise-authorisation-attempt
                              {:user/email-address "success+3@simulator.amazonses.com"
                               :authorisation/phrase "some-phrase"}}})
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

  (testing "The handler negotiates the finalise-authorisation-attempt command when the command is being
            made for an existing user and authorisation where the authorisation is grantable and no
            session is provided."
    (let [request (-> (h/request
                       {:command {:finalise-authorisation-attempt
                                  {:user/email-address "success+4@simulator.amazonses.com"
                                   :authorisation/phrase "some-phrase"}}})
                      (update :headers dissoc "cookie"))
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
      (is (some? (:authorisation/granted-at authorisation')))))

  (testing "The handler negotiates the finalise-authorisation-attempt command when the command is being
            made for an existing user and authorisation where the authorisation is grantable and an
            unauthorised session is provided."
    (let [request (h/request
                   {:command {:finalise-authorisation-attempt
                              {:user/email-address "success+5@simulator.amazonses.com"
                               :authorisation/phrase "some-phrase"}}})
          user-id (user/id "success+5@simulator.amazonses.com")
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
      (is (some? (:authorisation/granted-at authorisation')))))

  (testing "The handler negotiates the finalise-authorisation-attempt command when the command is being
            made for an existing user and authorisation where the authorisation is grantable and a session
            authorised to that user is provided."
    (let [request (h/request
                   {:cookie (h/cookie "success+6@simulator.amazonses.com")
                    :command {:finalise-authorisation-attempt
                              {:user/email-address "success+6@simulator.amazonses.com"
                               :authorisation/phrase "some-phrase"}}})
          user-id (user/id "success+6@simulator.amazonses.com")
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
      (is (some? (:authorisation/granted-at authorisation')))))

  (testing "The handler negotiates the finalise-authorisation-attempt command when the command is being
            made for an existing user and authorisation where the authorisation is grantable and a session
            authorised to a different user is provided."
    (let [request (h/request
                   {:cookie (h/cookie "success+6@simulator.amazonses.com")
                    :command {:finalise-authorisation-attempt
                              {:user/email-address "success+7@simulator.amazonses.com"
                               :authorisation/phrase "some-phrase"}}})
          user-id (user/id "success+7@simulator.amazonses.com")
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
