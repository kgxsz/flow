(ns flow.command
  (:require [flow.domain.authorisation :as authorisation]
            [flow.domain.user :as user]))


(defmulti handle first)


(defmethod handle :initialise-authorisation
  [[_ {:keys [authorisation-email-address]}]]
  (try
    (let [user-id (user/id authorisation-email-address)
          authorisation-phrase (authorisation/generate-phrase)]
      (if (user/fetch user-id)
        (do
          (authorisation/create user-id authorisation-phrase)
          (authorisation/send-phrase authorisation-email-address authorisation-phrase)
          {})
        {}))
    (catch Exception e
      {})))


;; TODO - convert all language here to an authorisation-attempt
(defmethod handle :finalise-authorisation
  [[_ {:keys [authorisation-email-address authorisation-phrase]}]]
  (try
    (let [user-id (user/id authorisation-email-address)
          authorisation-id (authorisation/id user-id authorisation-phrase)
          authorisation (authorisation/fetch authorisation-id)]
      (if (and (some? authorisation) (not (authorisation/expired? authorisation)))
        ;; TODO - user return IDs here consistently
        (do
          (authorisation/finalise authorisation-id)
          {:current-user-id user-id})
        {}))
    (catch Exception e
      {})))


(defmethod handle :add-user
  [[_ {:keys [user current-user-id]}]]
  (try
    (if (user/admin? (user/fetch current-user-id))
      (if-let [user-id (user/create (:email-address user)
                                    (:name user)
                                    (:roles user))]
        ;; TODO - probably want something more general here like a temp-id
        {:user-id user-id}
        {})
      {})
    (catch Exception e
      {})))


(defmethod handle :delete-user
  [[_ {:keys [user-id current-user-id]}]]
  (try
    (if (user/admin? (user/fetch current-user-id))
      (if-let [user-id (user/delete user-id)]
        {:user-id user-id}
        {})
      {})
    (catch Exception e
      {})))


(defmethod handle :deauthorise
  [command]
  {:current-user-id nil})


(defmethod handle :default
  [command]
  (throw (IllegalArgumentException. "Unsupported command method.")))
