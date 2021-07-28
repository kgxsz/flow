(ns flow.command.delete-user-test
  (:require [flow.core :refer :all]
            [flow.entity.authorisation :as authorisation]
            [flow.entity.user :as user]
            [flow.helpers :as h]
            [clojure.test :refer :all]))


(defn setup
  []
  (h/create-test-user! "success+1@simulator.amazonses.com")
  (h/create-test-user! "success+2@simulator.amazonses.com" "Test" #{:customer :admin})
  (h/create-test-user! "success+4@simulator.amazonses.com")
  (h/delete-test-user! "success+4@simulator.amazonses.com")
  (h/create-test-user! "success+5@simulator.amazonses.com")
  (h/create-test-user! "success+6@simulator.amazonses.com")
  (h/create-test-user! "success+7@simulator.amazonses.com" "Test" #{:customer :admin})
  (h/delete-test-user! "success+7@simulator.amazonses.com")
  (h/create-test-user! "success+8@simulator.amazonses.com")
  (h/create-test-user! "success+9@simulator.amazonses.com" "Test" #{:customer :admin}))

(defn fixture [test]
  (h/ensure-empty-table)
  (setup)
  (test)
  (h/ensure-empty-table))

(use-fixtures :each fixture)


(deftest test-delete-user

  (testing "The handler negotiates the delete-user command when no session is provided."
    (let [request (h/request
                   {:command {:delete-user {:user/id (user/id "success+1@simulator.amazonses.com")}}})
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
      (is (= user user'))))

  (testing "The handler negotiates the delete-user command when an unauthorised session is provided."
    (let [request (h/request
                   {:session :unauthorised
                    :command {:delete-user {:user/id (user/id "success+1@simulator.amazonses.com")}}})
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
      (is (= user user'))))

  (testing "The handler negotiates the delete-user command when the user being deleted does
            not exist and a session authorised to an admin user is provided."
    (let [request (h/request
                   {:session "success+2@simulator.amazonses.com"
                    :command {:delete-user {:user/id (user/id "success+3@simulator.amazonses.com")}}})
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
      (is (= nil user user'))))

  (testing "The handler negotiates the delete-user command when the user being deleted exists,
            has previously been deleted, and a session authorised to an admin user is provided."
    (let [request (h/request
                   {:session "success+2@simulator.amazonses.com"
                    :command {:delete-user {:user/id (user/id "success+4@simulator.amazonses.com")}}})
          user-id (user/id "success+4@simulator.amazonses.com")
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

  (testing "The handler negotiates the delete-user command when the user being deleted exists,
            has not previously been deleted, and a session authorised to a non-admin user is provided."
    (let [request (h/request
                   {:session "success+5@simulator.amazonses.com"
                    :command {:delete-user {:user/id (user/id "success+6@simulator.amazonses.com")}}})
          user-id (user/id "success+6@simulator.amazonses.com")
          user (user/fetch user-id)
          {:keys [status headers body] :as response} (handler request)
          user' (user/fetch user-id)]
      (is (= 200 status))
      (is (= {:users {}
              :authorisations {}
              :metadata {}
              :session {:current-user-id (user/id "success+5@simulator.amazonses.com")}}
             (h/decode :transit body)))
      (is (= user user'))))

  (testing "The handler negotiates the delete-user command when the user being deleted exists,
            has not previously been deleted, and a session authorised to an admin user is provided
            where that admin user has previously been deleted."
    (let [request (h/request
                   {:session "success+7@simulator.amazonses.com"
                    :command {:delete-user {:user/id (user/id "success+6@simulator.amazonses.com")}}})
          user-id (user/id "success+6@simulator.amazonses.com")
          user (user/fetch user-id)
          {:keys [status headers body] :as response} (handler request)
          user' (user/fetch user-id)]
      (is (= 200 status))
      (is (= {:users {}
              :authorisations {}
              :metadata {}
              :session {:current-user-id (user/id "success+7@simulator.amazonses.com")}}
             (h/decode :transit body)))
      (is (not= user user'))
      (is (nil? (:user/deleted-at user)))
      (is (some? (:user/deleted-at user')))))

  (testing "The handler negotiates the delete-user command when the user being deleted exists,
            has not previously been deleted, and a session authorised to an admin user is provided. "
    (let [request (h/request
                   {:session "success+2@simulator.amazonses.com"
                    :command {:delete-user {:user/id (user/id "success+8@simulator.amazonses.com")}}})
          user-id (user/id "success+8@simulator.amazonses.com")
          user (user/fetch user-id)
          {:keys [status headers body] :as response} (handler request)
          user' (user/fetch user-id)]
      (is (= 200 status))
      (is (= {:users {}
              :authorisations {}
              :metadata {}
              :session {:current-user-id (user/id "success+2@simulator.amazonses.com")}}
             (h/decode :transit body)))
      (is (not= user user'))
      (is (nil? (:user/deleted-at user)))
      (is (some? (:user/deleted-at user')))))

  (testing "The handler negotiates the delete-user command when the user being deleted exists,
            has not previously been deleted, and a session authorised for an admin user is provided
            where that admin user happens to be the same user being deleted."
    (let [request (h/request
                   {:session "success+9@simulator.amazonses.com"
                    :command {:delete-user {:user/id (user/id "success+9@simulator.amazonses.com")}}})
          user-id (user/id "success+9@simulator.amazonses.com")
          user (user/fetch user-id)
          {:keys [status headers body] :as response} (handler request)
          user' (user/fetch user-id)]
      (is (= 200 status))
      (is (= {:users {}
              :authorisations {}
              :metadata {}
              :session {:current-user-id (user/id "success+9@simulator.amazonses.com")}}
             (h/decode :transit body)))
      (is (not= user user'))
      (is (nil? (:user/deleted-at user)))
      (is (some? (:user/deleted-at user'))))))
