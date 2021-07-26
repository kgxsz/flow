(ns flow.error-handling-test
  (:require [flow.core :refer :all]
            [clojure.test :refer :all]
            [flow.helpers :as h]
            [slingshot.slingshot :as slingshot]))


(deftest test-error-handling

  (testing "The handler denies requests with unsupported request methods."
    (let [request (assoc (h/request) :request-method :get)
          {:keys [status headers body] :as response} (handler request)]
      (is (= 400 status))
      (is (= {"Content-Type" "application/json; charset=utf-8"}
             headers))
      (is (= {:error "Unsupported request method."} (h/decode :json body)))))

  (testing "The handler denies requests with unsupported request paths."
    (let [request (assoc (h/request) :uri "/hello")
          {:keys [status headers body] :as response} (handler request)]
      (is (= 400 status))
      (is (= {"Content-Type" "application/json; charset=utf-8"}
             headers))
      (is (= {:error "Unsupported request path."} (h/decode :json body)))))

  (testing "The handler denies requests with unsupported content type."
    (let [request (assoc (h/request) :headers {"content-type" "application/json"
                                             "accept" "application/transit+json"})
          {:keys [status headers body] :as response} (handler request)]
      (is (= 400 status))
      (is (= {"Content-Type" "application/json; charset=utf-8"}
             headers))
      (is (= {:error "Unsupported or missing Content-Type header."} (h/decode :json body)))))

  (testing "The handler denies requests with no content type declared."
    (let [request (assoc (h/request) :headers {"accept" "application/transit+json"})
          {:keys [status headers body] :as response} (handler request)]
      (is (= 400 status))
      (is (= {"Content-Type" "application/json; charset=utf-8"}
             headers))
      (is (= {:error "Unsupported or missing Content-Type header."} (h/decode :json body)))))

  (testing "The handler denies requests with malformed request content."
    (let [request (assoc (h/request) :body "hello world")
          {:keys [status headers body] :as response} (handler request)]
      (is (= 400 status))
      (is (= {"Content-Type" "application/json; charset=utf-8"}
             headers))
      (is (= {:error "Malformed application/transit+json content."} (h/decode :json body)))))

  (testing "The handler denies requests with invalid request content."
    (let [request (assoc (h/request) :body (h/encode :transit {:query {}}))
          {:keys [status headers body] :as response} (handler request)]
      (is (= 400 status))
      (is (= {"Content-Type" "application/json; charset=utf-8"}
             headers))
      (is (= {:error "Invalid request content."}
             (h/decode :json body)))))

  (testing "The handler provides specific information when an internal error occurs."
    (with-redefs [handle-query (fn [_] {:users {} :authorisations {} :session {}})]
      (let [{:keys [status headers body] :as response} (handler (h/request))]
        (is (= 500 status))
        (is (= {"Content-Type" "application/json; charset=utf-8"}
                 headers))
        (is (= {:error "The :response/body specification was violated."} (h/decode :json body))))))

  (testing "The handler provides partial information when an external error occurs."
    (with-redefs [handle-command (fn [_] (slingshot/throw+ {:type :flow/external-error}))]
      (let [{:keys [status headers body] :as response} (handler (h/request))]
        (is (= 500 status))
        (is (= {"Content-Type" "application/json; charset=utf-8"}
               headers))
        (is (= {:error "External error detected."} (h/decode :json body))))))

  (testing "The handler provides opaque information when an unspecified error occurs."
    (with-redefs [handle-command (fn [_] (throw (Exception. "hello world")))]
      (let [{:keys [status headers body] :as response} (handler (h/request))]
        (is (= 500 status))
        (is (= {"Content-Type" "application/json; charset=utf-8"}
               headers))
        (is (= {:error "Unspecified error detected."} (h/decode :json body)))))))
