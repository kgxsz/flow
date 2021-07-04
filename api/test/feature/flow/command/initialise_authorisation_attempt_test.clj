(ns flow.command.initialise-authorisation-attempt-test
  (:require [flow.core :refer :all]
            [flow.entity.authorisation :as authorisation]
            [flow.entity.user :as user]
            [flow.domain.user-management :as user-management]
            [flow.db :as db]
            [clojure.test :refer :all]
            [muuntaja.core :as muuntaja]
            [taoensso.faraday :as faraday]))


(def cookies
  {:authorised "session=A%2BxGyCLBttSGHCm7iyhCLY%2Bkbmnufu3s9O4%2FNjSewTmmUzoETzooY4pArKQULcpT08xBYGccImTug%2Bhex%2B3k772VwOFKoxw9YXRSJWTQsFs%3D--Xc5up%2BLiYmkuw7en8842HOwipzihtCSqlMOccqs4xTI%3D"
   :unauthorised "session=vJxkdtO0BOSz5nDqFHDhXAycccwHiYP6kHvsUQYb%2FUzrrj5jtUbM5obcG6htBG5W--ZO6plGXoqeDqtHlU38R2I3vwYsbt1YalbU9OEI0vq9Q%3D"})

(def encode (partial muuntaja/encode "application/transit+json"))
(def decode (partial muuntaja/decode "application/transit+json"))

(defn create-test-user!
  [email-address]
  (user/create! email-address "Test" #{:customer}))

(defn destroy-test-user!
  [email-address]
  (when-let [{:keys [user/id]} (user/fetch (user/id email-address))]
    (user/destroy! id)))

(defn delete-test-user!
  [email-address]
  (let [id (user/id email-address)]
    (user/mutate! id user-management/delete)))

(defn find-test-authorisations
  [email-address]
  (filter
   #(= (:user/id %) (user/id email-address))
   (authorisation/fetch-all)))

(defn destroy-test-authorisations!
  [email-address]
  (->> (find-test-authorisations email-address)
       (map :authorisation/id)
       (map authorisation/destroy!)
       (doall)))

(defn setup
  []
  (create-test-user! "success+1@simulator.amazonses.com")
  (create-test-user! "success+2@simulator.amazonses.com")
  (delete-test-user! "success+2@simulator.amazonses.com"))

(defn tear-down
  []
  (destroy-test-user! "success+1@simulator.amazonses.com")
  (destroy-test-user! "success+2@simulator.amazonses.com")
  (destroy-test-user! "success+3@simulator.amazonses.com")
  (destroy-test-authorisations! "success+1@simulator.amazonses.com")
  (destroy-test-authorisations! "success+2@simulator.amazonses.com")
  (destroy-test-authorisations! "success+3@simulator.amazonses.com"))

(defn fixture [f]
  (tear-down)
  (setup)
  (f)
  (tear-down))

(use-fixtures :once fixture)

(deftest test-initialise-authorisation-attempt

  (testing "The handler negotiates the initialise-authorisation-attempt command when the
            command is being made for an existing user."
    (let [request {:request-method :post
                   :uri "/"
                   :headers {"content-type" "application/transit+json"
                             "accept" "application/transit+json"
                             "cookie" (:unauthorised cookies)}
                   :body (encode {:command {:initialise-authorisation-attempt
                                            {:user/email-address "success+1@simulator.amazonses.com"}}
                                  :query {}
                                  :metadata {}
                                  :session {}})}
          {:keys [status headers body] :as response} (handler request)]
      (is (= 200 status))
      (is (= "application/transit+json; charset=utf-8" (get headers "Content-Type")))
      (is (= {:users {}
              :authorisations {}
              :metadata {}
              :session {:current-user-id nil}}
             (decode body)))
      (is (= 1 (count (find-test-authorisations "success+1@simulator.amazonses.com"))))))

  (testing "The handler negotiates the initialise-authorisation-attempt command when the
            command is being made for a deleted user."
    (let [request {:request-method :post
                   :uri "/"
                   :headers {"content-type" "application/transit+json"
                             "accept" "application/transit+json"
                             "cookie" (:unauthorised cookies)}
                   :body (encode {:command {:initialise-authorisation-attempt
                                            {:user/email-address "success+2@simulator.amazonses.com"}}
                                  :query {}
                                  :metadata {}
                                  :session {}})}
          {:keys [status headers body] :as response} (handler request)]
      (is (= 200 status))
      (is (= "application/transit+json; charset=utf-8" (get headers "Content-Type")))
      (is (= {:users {}
              :authorisations {}
              :metadata {}
              :session {:current-user-id nil}}
             (decode body)))
      (is (empty? (find-test-authorisations "success+2@simulator.amazonses.com")))))

  (testing "The handler negotiates the initialise-authorisation-attempt command when the
            command is being made for a non-existant user."
    (let [request {:request-method :post
                   :uri "/"
                   :headers {"content-type" "application/transit+json"
                             "accept" "application/transit+json"
                             "cookie" (:unauthorised cookies)}
                   :body (encode {:command {:initialise-authorisation-attempt
                                            {:user/email-address "success+3@simulator.amazonses.com"}}
                                  :query {}
                                  :metadata {}
                                  :session {}})}
          {:keys [status headers body] :as response} (handler request)]
      (is (= 200 status))
      (is (= "application/transit+json; charset=utf-8" (get headers "Content-Type")))
      (is (= {:users {}
              :authorisations {}
              :metadata {}
              :session {:current-user-id nil}}
             (decode body)))
      (is (empty? (find-test-authorisations "success+3@simulator.amazonses.com"))))))
