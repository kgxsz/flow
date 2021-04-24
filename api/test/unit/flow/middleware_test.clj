(ns flow.middleware-test
  (:require [flow.middleware :refer :all]
            [flow.specifications :as s]
            [clojure.test :refer :all]))


(def request
  {:request-method :post
   :uri ""})

(def response
  {:status 200
   :headers {}
   :body "{\"hello\": \"world\"}"})

(def handler (constantly response))


(deftest test-wrap-request-path

  (testing "The wrapped handler throws an exception when the request path isn't the root."
    (let [handler' (wrap-request-path handler)]
      (is (thrown? IllegalArgumentException (handler' (assoc request :uri "hello"))))
      (is (thrown? IllegalArgumentException (handler' (assoc request :uri "world"))))))

  (testing "The wrapped handler returns the response when the request path is the root."
    (let [handler' (wrap-request-path handler)]
      (is (= response (handler' request)))
      (is (= response (handler' (assoc request :uri "/")))))))


(deftest test-wrap-request-method

  (testing "The wrapped handler throws an exception when the request method isn't POST."
    (let [handler' (wrap-request-method handler)]
      (is (thrown? IllegalArgumentException (handler' (assoc request :request-method :get))))
      (is (thrown? IllegalArgumentException (handler' (assoc request :request-method :options))))
      (is (thrown? IllegalArgumentException (handler' (assoc request :request-method :head))))
      (is (thrown? IllegalArgumentException (handler' (assoc request :request-method :patch))))
      (is (thrown? IllegalArgumentException (handler' (assoc request :request-method :delete))))))

  (testing "The wrapped handler returns the response when the request method is POST."
    (let [handler' (wrap-request-method handler)]
      (is (= response (handler' request))))))
