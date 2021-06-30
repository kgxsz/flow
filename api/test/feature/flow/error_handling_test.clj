(ns flow.error-handling-test
  (:require [flow.core :refer :all]
            [clojure.test :refer :all]
            [muuntaja.core :as muuntaja]
            [slingshot.slingshot :as slingshot]
            [clojure.java.io :as io]))

(def encode (partial muuntaja/encode "application/transit+json"))

(defn request
  []
  {:request-method :post
   :uri "/"
   :headers {"content-type" "application/transit+json"
             "accept" "application/transit+json"}
   :body (encode {:command {} :query {} :metadata {} :session {}})})


(deftest test-error-handling

  (testing "The handler denies requests with unsupported request methods."
    (let [request (assoc (request) :request-method :get)
          {:keys [status headers body] :as response} (handler request)]
      (is (= 400 status))
      (is (= {"Content-Type" "application/json; charset=utf-8"}
             headers))
      (is (= {:error "Unsupported request method."}
             (muuntaja/decode "application/json" body)))))

  (testing "The handler denies requests with unsupported request paths."
    (let [request (assoc (request) :uri "/hello")
          {:keys [status headers body] :as response} (handler request)]
      (is (= 400 status))
      (is (= {"Content-Type" "application/json; charset=utf-8"}
             headers))
      (is (= {:error "Unsupported request path."}
             (muuntaja/decode "application/json" body)))))

  (testing "The handler denies requests with unsupported content type."
    (let [request (assoc (request) :headers {"content-type" "application/json"
                                             "accept" "application/transit+json"})
          {:keys [status headers body] :as response} (handler request)]
      (is (= 400 status))
      (is (= {"Content-Type" "application/json; charset=utf-8"}
             headers))
      (is (= {:error "Unsupported or missing Content-Type header."}
             (muuntaja/decode "application/json" body)))))

  (testing "The handler denies requests with no content type declared."
    (let [request (assoc (request) :headers {"accept" "application/transit+json"})
          {:keys [status headers body] :as response} (handler request)]
      (is (= 400 status))
      (is (= {"Content-Type" "application/json; charset=utf-8"}
             headers))
      (is (= {:error "Unsupported or missing Content-Type header."}
             (muuntaja/decode "application/json" body)))))

  (testing "The handler denies requests with malformed request content."
    (let [request (assoc (request) :body "hello world")
          {:keys [status headers body] :as response} (handler request)]
      (is (= 400 status))
      (is (= {"Content-Type" "application/json; charset=utf-8"}
             headers))
      (is (= {:error "Malformed application/transit+json content."}
             (muuntaja/decode "application/json" body)))))

  (testing "The handler denies requests with invalid request content."
    (let [request (assoc (request) :body (encode {:query {}}))
          {:keys [status headers body] :as response} (handler request)]
      (is (= 400 status))
      (is (= {"Content-Type" "application/json; charset=utf-8"}
             headers))
      (is (= {:error "Invalid request content."}
             (muuntaja/decode "application/json" body)))))

  (testing "The handler provides specific information when an internal error occurs."
    (with-redefs [handle-query (fn [_] {:users {} :authorisations {} :session {}})]
      (let [{:keys [status headers body] :as response} (handler (request))]
        (is (= 500 status))
        (is (= {"Content-Type" "application/json; charset=utf-8"}
                 headers))
        (is (= {:error "The :response/body specification was violated."}
               (muuntaja/decode "application/json" body))))))

  (testing "The handler provides partial information when an external error occurs."
    (with-redefs [handle-command (fn [_] (slingshot/throw+ {:type :flow/external-error}))]
      (let [{:keys [status headers body] :as response} (handler (request))]
        (is (= 500 status))
        (is (= {"Content-Type" "application/json; charset=utf-8"}
               headers))
        (is (= {:error "External error detected."}
               (muuntaja/decode "application/json" body))))))

  (testing "The handler provides opaque information when an unspecified error occurs."
    (with-redefs [handle-command (fn [_] (throw (Exception. "hello world")))]
      (let [{:keys [status headers body] :as response} (handler (request))]
        (is (= 500 status))
        (is (= {"Content-Type" "application/json; charset=utf-8"}
               headers))
        (is (= {:error "Unspecified error detected."}
               (muuntaja/decode "application/json" body)))))))
