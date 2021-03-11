(ns flow.command
  (:require [flow.entity.user :as user]
            [flow.entity.authorisation :as authorisation]
            [flow.domain.admin :as admin]
            [flow.domain.authorisation-attempt :as authorisation-attempt]))


(defmulti handle first)


(defmethod handle :initialise-authorisation
  [[_ {:keys [authorisation-email-address]}]]
  (try
    (let [user-id (user/id authorisation-email-address)
          authorisation-phrase (authorisation-attempt/generate-phrase)]
      (if (user/fetch user-id)
        (do
          (authorisation/create user-id authorisation-phrase)
          (authorisation-attempt/send-phrase authorisation-email-address authorisation-phrase)
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
      (if (and (some? authorisation) (not (authorisation-attempt/expired? authorisation)))
        ;; TODO - user return IDs here consistently
        (do
          (authorisation-attempt/finalise authorisation-id)
          {:current-user-id user-id})
        {}))
    (catch Exception e
      {})))


(defmethod handle :add-user
  [[_ {:keys [user current-user-id]}]]
  (try
    (if (admin/priveledged? (user/fetch current-user-id))
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
    (if (admin/priveledged? (user/fetch current-user-id))
      (if-let [user-id (admin/delete user-id)]
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
