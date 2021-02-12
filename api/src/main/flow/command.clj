(ns flow.command
  (:require [flow.domain.authorisation :as authorisation]
            [flow.domain.user :as user]))


(defmulti handle first)


(defmethod handle :initialise
  [[_ {:keys [current-user-id]}]]
  {})


(defmethod handle :initialise-authorisation
  [[_ {:keys [authorisation-email-address]}]]
  (try
    (let [user-id (user/id authorisation-email-address)
          authorisation-phrase (authorisation/generate-phrase)]
      (if (user/fetch user-id)
        (do
          (authorisation/create {:user-id user-id :phrase authorisation-phrase})
          (authorisation/send-phrase authorisation-email-address authorisation-phrase)
          {})
        {}))
    (catch Exception e
      {})))


(defmethod handle :finalise-authorisation
  [[_ {:keys [authorisation-email-address authorisation-phrase]}]]
  (try
    (let [user-id (user/id authorisation-email-address)
          authorisation-id (authorisation/id user-id authorisation-phrase)
          authorisation (authorisation/fetch authorisation-id)]
      (if (and (some? authorisation) (not (authorisation/expired? authorisation)))
        (do
          (authorisation/finalise authorisation-id)
          {:current-user-id user-id})
        {}))
    (catch Exception e
      {})))


(defmethod handle :deauthorise
  [command]
  {:current-user-id nil})


(defmethod handle :default
  [command]
  (throw (IllegalArgumentException. "Unsupported command method.")))
