(ns flow.session-test
  (:require [flow.core :refer :all]
            [flow.entity.authorisation :as authorisation]
            [flow.entity.user :as user]
            [flow.domain.user-management :as user-management]
            [flow.db :as db]
            [clojure.test :refer :all]
            [muuntaja.core :as muuntaja]))


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

(defn setup
  []
  (create-test-user! "success+1@simulator.amazonses.com"))

(defn tear-down
  []
  (destroy-test-user! "success+1@simulator.amazonses.com"))

(defn fixture [f]
  (tear-down)
  (setup)
  (f)
  (tear-down))

(use-fixtures :once fixture)


(deftest test-session

  (testing "The handler returns a session cookie and corresponding session when no cookie is provided."
    (let [request {:request-method :post
                   :uri "/"
                   :headers {"content-type" "application/transit+json"
                             "accept" "application/transit+json"}
                   :body (encode {:command {}
                                  :query {}
                                  :metadata {}
                                  :session {}})}
          {:keys [status headers body] :as response} (handler request)]
      (is (some? (get headers "Set-Cookie")))
      (is (= {:current-user-id nil} (:session (decode body))))))

  (testing "The handler returns a session cookie and corresponding session when a cookie
            containing an unauthorised session is provided."
    (let [request {:request-method :post
                   :uri "/"
                   :headers {"content-type" "application/transit+json"
                             "accept" "application/transit+json"
                             "cookie" (:unauthorised cookies)}
                   :body (encode {:command {}
                                  :query {}
                                  :metadata {}
                                  :session {}})}
          {:keys [status headers body] :as response} (handler request)]
      (is (some? (get headers "Set-Cookie")))
      (is (= {:current-user-id nil} (:session (decode body))))))

  (testing "The handler returns a session cookie and corresponding session when a cookie
            containing an authorised session is provided."
    (let [request {:request-method :post
                   :uri "/"
                   :headers {"content-type" "application/transit+json"
                             "accept" "application/transit+json"
                             "cookie" (:authorised cookies)}
                   :body (encode {:command {}
                                  :query {}
                                  :metadata {}
                                  :session {}})}
          {:keys [status headers body] :as response} (handler request)
          {:keys [session]} (decode body)]
      (is (some? (get headers "Set-Cookie")))
      (is (= {:current-user-id #uuid "fbc6d908-f74b-5dc7-9ff9-8c20323488a8"} session)))))
