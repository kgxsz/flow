(ns flow.middleware-test
  (:require [flow.middleware :refer :all]
            [flow.specifications :as s]
            [clojure.test :refer :all]))


(def request
  {:request-method :post
   :uri "/"
   :headers {"origin" "https://localhost:8080",
             "access-control-request-method" "POST"}})

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
      (is (= response (handler' (assoc request :uri "")))))))


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


(deftest test-wrap-exception

  (testing "The wrapped handler returns a 400 response when an illegal argument exception is thrown."
    (let [handler' (wrap-exception (fn [r] (throw (IllegalArgumentException. "hello world"))))]
      (is (= {:body "{\"error\": \"hello world\"}",
              :headers {"Content-Type" "application/json; charset=utf-8"},
              :status 400}
             (handler' request)))))

  (testing "The wrapped handler returns a 500 response when any other exception is thrown."
    (let [handler' (wrap-exception (fn [r] (throw (IllegalStateException. "hello world"))))]
      (is (= {:body "{\"error\": \"Internal error detected.\"}",
              :headers {"Content-Type" "application/json; charset=utf-8"},
              :status 500}
             (handler' request))))
    (let [handler' (wrap-exception (fn [r] (throw (Exception. "hello world"))))]
      (is (= {:body "{\"error\": \"Internal error detected.\"}",
              :headers {"Content-Type" "application/json; charset=utf-8"},
              :status 500}
             (handler' request)))))

  (testing "The wrapped handler returns the response when no exceptions are thrown."
    (let [handler' (wrap-exception handler)]
      (is (= response (handler' request))))))


(deftest test-wrap-cors

  (testing "The wrapped handler returns the preflight with CORS headers when the request method is OPTIONS."
    (let [handler' (wrap-cors handler)]
      (is (= {:body "preflight complete",
              :headers {"Access-Control-Allow-Credentials" "true",
                        "Access-Control-Allow-Headers" "Content-Type",
                        "Access-Control-Allow-Methods" "OPTIONS, POST",
                        "Access-Control-Allow-Origin" "https://localhost:8080"},
              :status 200}
             (handler' (assoc request :request-method :options))))))

  (testing "The wrapped handler returns no preflight with CORS headers when the origin isn't allowed."
    (let [handler' (wrap-cors handler)]
      (is (= response
             (handler' (-> request
                           (assoc :request-method :options)
                           (assoc-in [:headers "origin"] "hello-world")))))))

  (testing "The wrapped handler returns the response without CORS headers when the request method isn't allowed."
    (let [handler' (wrap-cors handler)]
      (is (= response
             (handler' (assoc request :request-method :get))))))

  (testing "The wrapped handler returns the response with CORS headers when the request method is allowed."
    (let [handler' (wrap-cors handler)]
      (is (= {:body "{\"hello\": \"world\"}"
              :headers {"Access-Control-Allow-Credentials" "true"
                        "Access-Control-Allow-Methods" "OPTIONS, POST"
                        "Access-Control-Allow-Origin" "https://localhost:8080"}
              :status 200}
             (handler' request))))))
