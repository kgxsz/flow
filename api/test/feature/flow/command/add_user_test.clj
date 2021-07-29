(ns flow.command.add-user-test
  (:require [flow.core :refer :all]
            [flow.entity.authorisation :as authorisation]
            [flow.entity.user :as user]
            [flow.helpers :as h]
            [clojure.test :refer :all]))


(defn setup
  []
  (h/create-test-user! "success+2@simulator.amazonses.com" "Test" #{:customer :admin})
  (h/create-test-user! "success+3@simulator.amazonses.com")
  (h/create-test-user! "success+4@simulator.amazonses.com" "Test" #{:customer :admin})
  (h/delete-test-user! "success+4@simulator.amazonses.com"))

(defn fixture [test]
  (h/ensure-empty-table)
  (setup)
  (test)
  (h/ensure-empty-table))

(use-fixtures :each fixture)


(deftest test-add-user

  (testing "The handler negotiates the add-user command when no session is provided."
    (let [request (h/request
                   {:command {:add-user
                              {:user/id #uuid "00000000-0000-0000-0000-000000000000"
                               :user/email-address "success+1@simulator.amazonses.com"
                               :user/name "Test"
                               :user/roles #{:customer}}}})
          user-id (user/id "success+1@simulator.amazonses.com")
          user (user/fetch user-id)
          {:keys [status headers body] :as response} (handler request)
          user' (user/fetch user-id)]
      (is (= 200 status))
      (is (= {:users {}
              :authorisations {}
              :metadata {}
              :session {:current-user-id nil}}
             (h/decode :transit body)))
      (is (= nil user user'))))

  (testing "The handler negotiates the add-user command when an unauthorised session is provided."
    (let [request (h/request
                   {:session :unauthorised
                    :command {:add-user
                              {:user/id #uuid "00000000-0000-0000-0000-000000000000"
                               :user/email-address "success+1@simulator.amazonses.com"
                               :user/name "Test"
                               :user/roles #{:customer}}}})
          user-id (user/id "success+1@simulator.amazonses.com")
          user (user/fetch user-id)
          {:keys [status headers body] :as response} (handler request)
          user' (user/fetch user-id)]
      (is (= 200 status))
      (is (= {:users {}
              :authorisations {}
              :metadata {}
              :session {:current-user-id nil}}
             (h/decode :transit body)))
      (is (= nil user user'))))

  (testing "The handler negotiates the add-user command when the command is being made for an
            exiting user and a session authorised to a user with an admin role is provided."
    (let [request (h/request
                   {:session "success+2@simulator.amazonses.com"
                    :command {:add-user
                              {:user/id #uuid "00000000-0000-0000-0000-000000000000"
                               :user/email-address "success+3@simulator.amazonses.com"
                               :user/name "Test"
                               :user/roles #{:customer}}}})
          user-id (user/id "success+3@simulator.amazonses.com")
          user (user/fetch user-id)
          {:keys [status headers body] :as response} (handler request)
          user' (user/fetch user-id)]
      (is (= 200 status))
      (is (= {:users {}
              :authorisations {}
              :metadata {}
              :session {:current-user-id (user/id "success+2@simulator.amazonses.com")}}
             (h/decode :transit body)))
      (is (= user user'))))

  (testing "The handler negotiates the add-user command when the command is being made for a
            non-existent user and a session authorised to a user without an admin role is provided."
    (let [request (h/request
                   {:session "success+3@simulator.amazonses.com"
                    :command {:add-user
                              {:user/id #uuid "00000000-0000-0000-0000-000000000000"
                               :user/email-address "success+1@simulator.amazonses.com"
                               :user/name "Test"
                               :user/roles #{:customer}}}})
          user-id (user/id "success+1@simulator.amazonses.com")
          user (user/fetch user-id)
          {:keys [status headers body] :as response} (handler request)
          user' (user/fetch user-id)]
      (is (= 200 status))
      (is (= {:users {}
              :authorisations {}
              :metadata {}
              :session {:current-user-id (user/id "success+3@simulator.amazonses.com")}}
             (h/decode :transit body)))
      (is (= nil user user'))))

  (testing "The handler negotiates the add-user command when the command is being made for a
            non-existent user and a session authorised to a user with an admin role is provided,
            where that authorised user has previously been deleted."
    (let [request (h/request
                   {:session "success+4@simulator.amazonses.com"
                    :command {:add-user
                              {:user/id #uuid "00000000-0000-0000-0000-000000000000"
                               :user/email-address "success+5@simulator.amazonses.com"
                               :user/name "Test"
                               :user/roles #{:customer}}}})
          user-id (user/id "success+5@simulator.amazonses.com")
          user (user/fetch user-id)
          {:keys [status headers body] :as response} (handler request)
          user' (user/fetch user-id)]
      (is (= 200 status))
      (is (= {:users {}
              :authorisations {}
              :metadata {:id-resolution
                         {#uuid "00000000-0000-0000-0000-000000000000" user-id}}
              :session {:current-user-id (user/id "success+4@simulator.amazonses.com")}}
             (h/decode :transit body)))
      (is (nil? user))
      (is (some? user'))))

  (testing "The handler negotiates the add-user command when the command is being made for a
            non-existent user and a session authorised to a user with an admin role is provided."
    (let [request (h/request
                   {:session "success+2@simulator.amazonses.com"
                    :command {:add-user
                              {:user/id #uuid "00000000-0000-0000-0000-000000000000"
                               :user/email-address "success+6@simulator.amazonses.com"
                               :user/name "Test"
                               :user/roles #{:customer}}}})
          user-id (user/id "success+6@simulator.amazonses.com")
          user (user/fetch user-id)
          {:keys [status headers body] :as response} (handler request)
          user' (user/fetch user-id)]
      (is (= 200 status))
      (is (= {:users {}
              :authorisations {}
              :metadata {:id-resolution
                         {#uuid "00000000-0000-0000-0000-000000000000" user-id}}
              :session {:current-user-id (user/id "success+2@simulator.amazonses.com")}}
             (h/decode :transit body)))
      (is (nil? user))
      (is (some? user')))))
