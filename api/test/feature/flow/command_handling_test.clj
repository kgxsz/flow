(ns flow.command-handling-test
  (:require [flow.core :refer :all]
            [flow.entity.authorisation :as authorisation]
            [flow.entity.user :as user]
            [flow.db :as db]
            [clojure.test :refer :all]
            [muuntaja.core :as muuntaja]
            [taoensso.faraday :as faraday]
            [clojure.java.io :as io]))


(def cookies
  {:authorised "session=FtMtcFDjknsAyWJyaXtF3GKRVybJkeya7tEJ3G1zfRSxmOYIR36Ivz4QMyqROOcYTML99NXXlA5vkrTZWoMxrbvGCx%2Fmo%2FR0InWbjnPPfe0%3D--EulONdtIRGbl54TK2J5fvw2%2FOnQGJCLJ8tOvb0rlR8M%3Dn"
   :unauthorised "session=vJxkdtO0BOSz5nDqFHDhXAycccwHiYP6kHvsUQYb%2FUzrrj5jtUbM5obcG6htBG5W--ZO6plGXoqeDqtHlU38R2I3vwYsbt1YalbU9OEI0vq9Q%3D"})

(def encode (partial muuntaja/encode "application/transit+json"))
(def decode (partial muuntaja/decode "application/transit+json"))

(defn request
  []
  {:request-method :post
   :uri "/"
   :headers {"content-type" "application/transit+json"
             "accept" "application/transit+json"}
   :body (encode {:command {} :query {} :metadata {} :session {}})})


;; TODO
;; 1. Create a test user with test email address
;; 2. Run tests
;; 3. Tear down the test user and any associated entities

#_(defn my-test-fixture [f]
  (user/create! "success@simulator.amazonses.com" "Testy" #{:customer})
  (f)
  (faraday/delete-item db/config :flow {:partition "user:be1ca27b-6e34-5957-9f0f-1105baf04c0c"})
  )

#_(defn something  []
  (->> (authorisation/fetch-all)
       (filter #(= (:user/id %) #uuid "be1ca27b-6e34-5957-9f0f-1105baf04c0c"))
       (map :authorisation/id)
       ))

#_(use-fixtures :once my-test-fixture)

(deftest test-initialise-authorisation-attempt

  (testing "The handler negotiates the TODO command."
    (let [request {:request-method :post
                   :uri "/"
                   :headers {"content-type" "application/transit+json"
                             "accept" "application/transit+json"
                             "cookie" (:unauthorised cookies)}
                   :body (encode {:command {:initialise-authorisation-attempt
                                            {:user/email-address "success@simulator.amazonses.com"}}
                                  :query {}
                                  :metadata {}
                                  :session {}})}
          {:keys [status headers body] :as response} (handler request)]
      (is (= 200 status))
      (is (= "application/transit+json; charset=utf-8"
             (get headers "Content-Type")))
      (is (some? (get headers "Set-Cookie")))
      (is (= {:users {}
              :authorisations {}
              :metadata {}
              :session {:current-user-id nil}}
             (decode body)))))
  )
