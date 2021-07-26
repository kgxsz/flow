(ns flow.helpers
  (:require [flow.core :refer :all]
            [flow.entity.authorisation :as authorisation]
            [flow.entity.user :as user]
            [flow.domain.user-management :as user-management]
            [flow.domain.authorisation-attempt :as authorisation-attempt]
            [flow.db :as db]
            [clj-time.coerce :as t.coerce]
            [clj-time.core :as t]
            [muuntaja.core :as muuntaja]))


(defn cookie
  "Helper function for generating a cookie for an authorised session for the email
   address provided. If the email address provided does not exist, falls back to
   providing a cookie for an unauthorised session."
  ([] (cookie nil))
  ([email-address]
   (case email-address
     "success+1@simulator.amazonses.com"
     "session=A%2BxGyCLBttSGHCm7iyhCLY%2Bkbmnufu3s9O4%2FNjSewTmmUzoETzooY4pArKQULcpT08xBYGccImTug%2Bhex%2B3k772VwOFKoxw9YXRSJWTQsFs%3D--Xc5up%2BLiYmkuw7en8842HOwipzihtCSqlMOccqs4xTI%3D"
     "success+2@simulator.amazonses.com"
     "session=mHw90D%2F2WHC4rrQQ67MoDxJHQ7G6i4%2F9eOOfmKeWNeaiesAltp%2BCFHgszvO4Of%2Bzje6HM8KPolW1UQqr0x8vIBesucLO7pK9yQsxvAA3%2B74%3D--wnbObmTg5L6ppu51ilT3gBFOIYqAEjKRsIrA4MaeXTQ%3D"
     "success+3@simulator.amazonses.com"
     "session=ZOpL2Z1Igv3rb3I9eNy78sOSE1CfMQ0mXEYmYjX6AVxk3VZ46zfyu3QbV%2BL5soHnFhFutL4LbLf%2BsyfmCwzZ17u37qQM8HKXnzpmgQYcdVc%3D--WTMqeApWc5wEsNb8fDGYDZtqFq1HXk80%2BCpQYKWaAww%3D"
     "success+4@simulator.amazonses.com"
     "session=9OtgYnRnAk6v8D%2Fa5HGax%2FvS4CopNQzi7IwkCmZIX%2FGnndokqBGlhgz4zKKxWEdsG2%2BGvQRkl8eoQ8rlzNz0a746Ml1gFDnHshxJaw2M7jM%3D--RhhFa3mW9riWoKFHgwRlmS%2B3sRw7ROKekyESPW4Hu%2Bw%3D"
     "session=vJxkdtO0BOSz5nDqFHDhXAycccwHiYP6kHvsUQYb%2FUzrrj5jtUbM5obcG6htBG5W--ZO6plGXoqeDqtHlU38R2I3vwYsbt1YalbU9OEI0vq9Q%3D")))


(defn encode
  "Encodes the content in either transit or json."
  [type content]
  (muuntaja/encode
   (case type
     :transit "application/transit+json"
     :json "application/json")
   content))


(defn decode
  "Decodes the content in either transit or json."
  [type content]
  (muuntaja/decode
   (case type
     :transit "application/transit+json"
     :json "application/json")
   content))


(defn request
  "Helper function for constructing requests made to the core handler."
  ([] (request {}))
  ([request]
   {:request-method :post
    :uri "/"
    :headers {"content-type" "application/transit+json"
              "accept" "application/transit+json"
              "cookie" (or (:cookie request) (cookie))}
    :body (encode
           :transit
           {:command (:command request {})
            :query (:query request {})
            :metadata {}
            :session {}})}))


(defn create-test-user!
  "Creates a test user with customer role."
  [email-address]
  (user/create! email-address "Test" #{:customer}))


(defn destroy-test-user!
  "Destroys the test user."
  [email-address]
  (when-let [{:keys [user/id]} (user/fetch (user/id email-address))]
    (user/destroy! id)))


(defn delete-test-user!
  "Manipulates the test user so that it is marked as deleted."
  [email-address]
  (let [id (user/id email-address)]
    (user/mutate! id user-management/delete)))


(defn create-test-authorisation!
  "Creates a test authorisation."
  [user-id phrase]
  (authorisation/create! user-id phrase))


(defn age-test-authorisation!
  "Manipulates the test authorisations so that it is marked as created
   five minutes earlier than it actually was."
  [user-id phrase]
  (let [id (authorisation/id user-id phrase)]
    (authorisation/mutate!
     id
     (fn [{:keys [authorisation/created-at] :as authorisation}]
       (assoc authorisation
              :authorisation/created-at
              (-> (t.coerce/from-date created-at)
                  (t/minus (t/minutes 5))
                  (t.coerce/to-date)))))))


(defn grant-test-authorisation!
  "Manipulates the test authorisations so that it is marked as granted."
  [user-id phrase]
  (let [id (authorisation/id user-id phrase)]
    (authorisation/mutate! id authorisation-attempt/grant)))


(defn find-test-authorisations
  "Helper function to find all test authorisations associated with
   the user id provided."
  [user-id]
  (filter
   #(= (:user/id %) user-id)
   (authorisation/fetch-all)))


(defn destroy-test-authorisations!
  "Destroys all test authorisations associarted with the user id provided."
  [user-id]
  (->> (find-test-authorisations user-id)
       (map :authorisation/id)
       (map authorisation/destroy!)
       (doall)))
