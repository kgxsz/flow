(ns flow.cross-origin-resource-sharing-test
  (:require [flow.core :refer :all]
            [clojure.test :refer :all]
            [muuntaja.core :as muuntaja]
            [clojure.java.io :as io]))


(deftest test-cross-origin-resource-sharing

  (testing "The handler accepts the cross origin resource sharing request when
            correct headers are provided."
    (let [request {:request-method :options
                   :headers {"origin" "https://localhost:8080"
                             "access-control-request-method" "POST"
                             "accept" "application/transit+json"}}
          {:keys [status headers body] :as response} (handler request)]
      (is (= 200 status))
      (is (= {"Access-Control-Allow-Credentials" "true",
              "Access-Control-Allow-Headers" "Content-Type",
              "Access-Control-Allow-Methods" "OPTIONS, POST",
              "Access-Control-Allow-Origin" "https://localhost:8080"}
             headers))
      (is (= "preflight complete" body))))

  (testing "The handler denies the cross origin resource sharing request when
            incorrect headers are provided."
    (let [request {:request-method :options
                   :headers {"origin" "https://helloworld:8080"
                             "access-control-request-method" "POST"
                             "accept" "application/transit+json"}}
          {:keys [status headers body] :as response} (handler request)]
      (is (= 400 status))
      (is (= {"Content-Type" "application/json; charset=utf-8"}
             headers))
      (is (= {:error "Unsupported request method."}
             (muuntaja/decode "application/json" body))))))
