(ns flow.middleware-test
  (:require [flow.middleware :refer :all]
            [flow.specifications :as s]
            [flow.utils :as u]
            [muuntaja.core :as muuntaja]
            [clojure.test :refer :all]))


(def request
  {:request-method :post
   :uri "/"
   :body "[\"^ \",\"~:query\",[\"^ \",\"~:users\",[\"^ \"]]]"
   :headers {"origin" "https://localhost:8080"
             "access-control-request-method" "POST"
             "content-type" "application/transit+json"
             "accept" "application/transit+json"}})

(def response
  {:status 200
   :headers {}
   :body {:users {}}})

(def handler (constantly response))


(deftest test-wrap-session

  (testing "The wrapped handler returns a response with a cookie header when the session is non nil."
    (let [handler' (wrap-session (constantly (assoc response :session {:hello "world"})))]
      (is (some? (get-in (handler' request) [:headers "Set-Cookie"])))))

  (testing "The wrapped handler returns a response without a cookie header when the session is nil."
    (let [handler' (wrap-session (constantly (assoc response :session nil)))]
      (is (nil? (get-in (handler' request) [:headers "Set-Cookie"]))))))


(deftest test-wrap-content-validation

  (testing "The wrapped handler throws an exception when the request's content is invalid."
    (let [handler' (wrap-content-validation handler)]
      (is (thrown? IllegalArgumentException
                   (handler' (assoc request :body-params {:hello "world"}))))
      (is (thrown? IllegalArgumentException
                   (handler' (assoc request :body-params ""))))
      (is (thrown? IllegalArgumentException
                   (handler' request)))))

  (testing "The wrapped handler throws an exception when the response's content is invalid."
    (let [handler' (wrap-content-validation handler)]
      (with-redefs [u/validate (fn [_ _] (throw (IllegalStateException. "hello world")))]
        (is (thrown? IllegalStateException
                     (handler' (assoc request :body-params {:query {:users {}}})))))))

  (testing "The wrapped handler returns the response when both request and response content are valid."
    (let [handler' (wrap-content-validation handler)]
      (with-redefs [u/validate (constantly (:body response))]
        (is (= response
               (handler' (assoc request :body-params {:query {:users {}}}))))))))


(deftest test-wrap-content-type

  (testing "The wrapped handler throws an exception when the request's content type header isn't
            transit."
    (let [handler' (wrap-content-type handler)]
      (is (thrown? IllegalArgumentException
                   (handler' (assoc-in request [:headers "content-type"] "application/json"))))
      (is (thrown? IllegalArgumentException
                   (handler' (update request :headers dissoc "content-type"))))))

  (testing "The wrapped handler throws an exception when the request's body cannot be decoded using
            the content type."
    (let [handler' (wrap-content-type handler)]
      (is (thrown? IllegalArgumentException
                   (handler' (assoc-in request [:headers "content-type"] "application/json"))))
      (is (thrown? IllegalArgumentException
                   (handler' (assoc request :body "hello world"))))))

  (testing "The wrapped handler returns the response body encoded with request's accept header."
    (let [handler' (wrap-content-type handler)]
      (is (= "[\"^ \",\"~:users\",[\"^ \"]]"
             (-> (handler' request) (:body) (slurp))))))

  (testing "The wrapped handler returns the response headers with content type equivalent to the
            request's accept header."
    (let [handler' (wrap-content-type handler)]
      (is (= {"Content-Type" "application/transit+json; charset=utf-8"}
             (:headers (handler' request)))))
    (let [handler' (wrap-content-type handler)]
      (is (= {"Content-Type" "application/json; charset=utf-8"}
             (:headers (handler' (assoc-in request [:headers "accept"] "application/json")))))))

  (testing "The wrapped handler returns the response body as is if it cannot be encoded with
            request's accept header."
    (let [handler' (wrap-content-type (constantly {:status 200 :headers {} :body "hello-world"}))]
      (is (= "hello-world"
             (:body (handler' request))))))

  (testing "The wrapped handler returns the response with no contenty type header if it cannot
            encode the response body with request's accept header."
    (let [handler' (wrap-content-type (constantly {:status 200 :headers {} :body "hello-world"}))]
      (is (= {}
             (:headers (handler' request)))))))


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
    (let [handler' (wrap-exception (fn [_] (throw (IllegalArgumentException. "hello world"))))]
      (is (= {:body "{\"error\": \"hello world\"}",
              :headers {"Content-Type" "application/json; charset=utf-8"},
              :status 400}
             (handler' request)))))

  (testing "The wrapped handler returns a 500 response when any other exception is thrown."
    (let [handler' (wrap-exception (fn [_] (throw (IllegalStateException. "hello world"))))]
      (is (= {:body "{\"error\": \"Internal error detected.\"}",
              :headers {"Content-Type" "application/json; charset=utf-8"},
              :status 500}
             (handler' request))))
    (let [handler' (wrap-exception (fn [_] (throw (Exception. "hello world"))))]
      (is (= {:body "{\"error\": \"Internal error detected.\"}",
              :headers {"Content-Type" "application/json; charset=utf-8"},
              :status 500}
             (handler' request)))))

  (testing "The wrapped handler returns the response when no exceptions are thrown."
    (let [handler' (wrap-exception handler)]
      (is (= response (handler' request))))))


(deftest test-wrap-cors

  (testing "The wrapped handler returns a successful preflight response with CORS headers when the
            request method is OPTIONS."
    (let [handler' (wrap-cors handler)]
      (is (= {:body "preflight complete",
              :headers {"Access-Control-Allow-Credentials" "true",
                        "Access-Control-Allow-Headers" "Content-Type",
                        "Access-Control-Allow-Methods" "OPTIONS, POST",
                        "Access-Control-Allow-Origin" "https://localhost:8080"},
              :status 200}
             (handler' (assoc request :request-method :options))))))

  (testing "The wrapped handler returns a non-preflight response, and no CORS headers when the request
            method is OPTIONS, but origin isn't allowed."
    (let [handler' (wrap-cors handler)]
      (is (= response
             (handler' (-> request
                           (assoc :request-method :options)
                           (assoc-in [:headers "origin"] "hello-world")))))))

  (testing "The wrapped handler returns the response without CORS headers when the request method
            isn't one of methods with access control."
    (let [handler' (wrap-cors handler)]
      (is (= response
             (handler' (assoc request :request-method :get))))
      (is (= response
             (handler' (assoc request :request-method :head))))))

  (testing "The wrapped handler returns the response with CORS headers when the request method has
            access control."
    (let [handler' (wrap-cors handler)]
      (is (= {"Access-Control-Allow-Credentials" "true"
              "Access-Control-Allow-Methods" "OPTIONS, POST"
              "Access-Control-Allow-Origin" "https://localhost:8080"}
             (:headers (handler' request)))))))
