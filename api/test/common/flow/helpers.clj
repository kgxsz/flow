(ns flow.helpers
  (:require [flow.core :refer :all]
            [flow.entity.authorisation :as authorisation]
            [flow.entity.user :as user]
            [flow.domain.user-management :as user-management]
            [flow.domain.authorisation-attempt :as authorisation-attempt]
            [flow.db :as db]
            [taoensso.faraday :as faraday]
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
     "success+5@simulator.amazonses.com"
     "session=LYjBHU3v3GQLGiG66OOsGmWTxTXJs%2FqlGRAeiIC7WTBAayJoH4aZJamDEfCNAbNMCP%2BVE81KPdVp0xVA6CkYYL2kZkmb2tn0QEhbrXBKrUA%3D--inPU6KDNaFrIpNoWmgDWiTMIuIphD7H%2FeL4CcR3Aghc%3D"
     "success+6@simulator.amazonses.com"
     "session=SfPbZmi6kfuJ6JulOMhKxYlvwZf0j6oA0KvAJY7nqP33F36%2Fd5OBZcF0ee%2FJRg9wQctl0%2FJJo%2FNaZ8eoMLMVxvQ%2BllCwgSkaqdf5CzLhkm0%3D--9DrFbAZ9oOAhagNvXp3hJvUXBvljoXBOYx2X45HN52U%3D"
     "success+7@simulator.amazonses.com"
     "session=I2kNmS%2Fo3v6uNGcY9YjTwibFASsTe9pHzWWj2nszOx%2FB4EKne6Tv69pJ0tczMcBGvWnoaghUSTWUik3VeD9%2F4V72vpB8I9rJApf4Dt9lTzk%3D--VqTALN5K4dUyeIIbvG33qfBpP6CSegLPLODuzkvuQ%2FU%3D"
     "success+8@simulator.amazonses.com"
     "session=5CApdiuoLs9i0D5dM12BezCJ8N0qbKTs9ZaflO55JOQu83vAM9bukYuGYX0nzLsUAVjEZ2KZVD8k4d4PAYWiq0DIS46HqASYCvugN9wPabU%3D--mucm9C0INYo4Lh0%2FXTWymteyKPTJ2A%2FjpEEYRIR4cyA%3D"
     "success+9@simulator.amazonses.com"
     "session=xYIlw62UvxRpheDcPPN%2FbPe3hM0v5Kc2J6zmGcTEYv%2BEO%2BGDRgu8F75jTnsb3spqQ3Nhq6BFY3nDjcGPz80pK0CnNQ0arNiVYvOfLJPY8Qs%3D--WvOWmN1tZkqqCT76gGiNKW7I5reU76zkTepcsPfTllQ%3D"
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
  "Helper function for constructing requests made to the core handler.
   Options can override the command and query, as well as set either
   a unauthorised session cookie, or set the session to one of the
   users by providing the email addresses."
  ([] (request {}))
  ([{:keys [method session command query]}]
   {:request-method (or method :post)
    :uri "/"
    :headers (cond-> {"content-type" "application/transit+json"
                      "accept" "application/transit+json"}
               session (assoc "cookie" (if (= :unauthorised session)
                                         (cookie)
                                         (cookie session))))
    :body (encode
           :transit
           {:command (or command {})
            :query (or query {})
            :metadata {}
            :session {}})}))


(defn ensure-empty-table
  "Helper function for clearing the table so as to have a clean
   an predictable table for each test."
  []
  (let [table-description (faraday/describe-table db/config :flow)
        table-index [:partition :s]
        table-options {:throughput {:read 1 :write 1} :block? true}]
    (when (some? table-description)
      (faraday/delete-table db/config :flow))
    (faraday/create-table db/config :flow table-index table-options)))


(defn create-test-user!
  "Creates a test user with some default options unless specified otherwise."
  ([email-address]
   (user/create! email-address "Test" #{:customer}))
  ([email-address name roles]
   (user/create! email-address name roles)))


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
