(ns flow.command.deauthorise-test
  (:require [flow.core :refer :all]
            [flow.entity.authorisation :as authorisation]
            [flow.entity.user :as user]
            [flow.helpers :as h]
            [clojure.test :refer :all]))


(defn setup
  []
  (h/create-test-user! "success+1@simulator.amazonses.com"))

(defn fixture [test]
  (h/ensure-empty-table)
  (setup)
  (test)
  (h/ensure-empty-table))

(use-fixtures :each fixture)


(deftest test-deauthorise

  (testing "The handler negotiates the deauthorise command when no session is provided."
    (let [request (h/request
                   {:command {:deauthorise {}}})
          {:keys [status headers body] :as response} (handler request)]
      (is (= 200 status))
      (is (= {:users {}
              :authorisations {}
              :metadata {}
              :session {:current-user-id nil}}
             (h/decode :transit body)))))

  (testing "The handler negotiates the deauthorise command when an unauthorised session is provided."
    (let [request (h/request
                   {:session :unauthorised
                    :command {:deauthorise {}}})
          {:keys [status headers body] :as response} (handler request)]
      (is (= 200 status))
      (is (= {:users {}
              :authorisations {}
              :metadata {}
              :session {:current-user-id nil}}
             (h/decode :transit body)))))

  (testing "The handler negotiates the deauthorise command when an authorised session is provided."
    (let [request (h/request
                   {:session "success+1@simulator.amazonses.com"
                    :command {:deauthorise {}}})
          {:keys [status headers body] :as response} (handler request)]
      (is (= 200 status))
      (is (= {:users {}
              :authorisations {}
              :metadata {}
              :session {:current-user-id nil}}
             (h/decode :transit body))))))
