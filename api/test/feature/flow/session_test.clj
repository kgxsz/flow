(ns flow.session-test
  (:require [flow.core :refer :all]
            [flow.entity.user :as user]
            [flow.helpers :as h]
            [clojure.test :refer :all]))


(defn setup
  []
  (h/create-test-user! "success+1@simulator.amazonses.com"))

(defn tear-down
  []
  (h/destroy-test-user! "success+1@simulator.amazonses.com"))

(defn fixture [test]
  (tear-down)
  (setup)
  (test)
  (tear-down))

(use-fixtures :each fixture)


(deftest test-session

  (testing "The handler returns a session cookie and corresponding session when no cookie is provided."
    (let [request {:request-method :post
                   :uri "/"
                   :headers {"content-type" "application/transit+json"
                             "accept" "application/transit+json"}
                   :body (h/encode
                          :transit
                          {:command {}
                           :query {}
                           :metadata {}
                           :session {}})}
          {:keys [status headers body] :as response} (handler request)]
      (is (some? (get headers "Set-Cookie")))
      (is (= {:current-user-id nil} (:session (h/decode :transit body))))))

  (testing "The handler returns a session cookie and corresponding session when a cookie
            containing an unauthorised session is provided."
    (let [request {:request-method :post
                   :uri "/"
                   :headers {"content-type" "application/transit+json"
                             "accept" "application/transit+json"
                             "cookie" (h/cookie)}
                   :body (h/encode
                          :transit
                          {:command {}
                           :query {}
                           :metadata {}
                           :session {}})}
          {:keys [status headers body] :as response} (handler request)]
      (is (some? (get headers "Set-Cookie")))
      (is (= {:current-user-id nil} (:session (h/decode :transit body))))))

  (testing "The handler returns a session cookie and corresponding session when a cookie
            containing an authorised session is provided."
    (let [request {:request-method :post
                   :uri "/"
                   :headers {"content-type" "application/transit+json"
                             "accept" "application/transit+json"
                             "cookie" (h/cookie "success+1@simulator.amazonses.com")}
                   :body (h/encode
                          :transit
                          {:command {}
                           :query {}
                           :metadata {}
                           :session {}})}
          {:keys [status headers body] :as response} (handler request)
          {:keys [session]} (h/decode :transit body)]
      (is (some? (get headers "Set-Cookie")))
      (is (= {:current-user-id (user/id "success+1@simulator.amazonses.com")} session)))))
