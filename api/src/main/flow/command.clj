(ns flow.command
  (:require [flow.entity.user :as user]
            [flow.entity.authorisation :as authorisation]
            [flow.domain.user-management :as user-management]
            [flow.domain.authorisation-attempt :as authorisation-attempt]))


(defmulti handle (fn [method payload metadata] method))


(defmethod handle :initialise-authorisation-attempt
  [_ {:keys [user/email-address]} _]

  "If the user with the given email address exists and has not been deleted,
   then a phrase will be generated, an authorisation will be created, and an
   email containing the phrase will be sent to the user such that they may
   finalise their authorisation attempt."

  ;; TODO - this needs to move into a proper spec
  (when-not (string? email-address)
    (throw (IllegalArgumentException. "Unsupported command parameters.")))

  (let [{:user/keys [id deleted-at] :as user} (user/fetch (user/id email-address))]
    (if (and (some? user) (nil? deleted-at))
      (let [phrase (authorisation-attempt/generate-phrase)]
        (authorisation/create! id phrase)
        (authorisation-attempt/send-phrase! email-address phrase)
        {})
      {})))


(defmethod handle :finalise-authorisation-attempt
  [_ {:keys [user/email-address authorisation/phrase]} _]

  "If a grantable authorisation is found to match the given email address and phrase,
   then the authorisation will be marked as granted, and a session will be created."

  ;; TODO - this needs to move into a proper spec
  (when-not (and (string? email-address)
                 (string? phrase))
    (throw (IllegalArgumentException. "Unsupported command parameters.")))

  (let [user (user/fetch (user/id email-address))
        authorisation (authorisation/fetch (authorisation/id (:user/id user) phrase))]
    (if (and (some? authorisation) (authorisation-attempt/grantable? authorisation))
      (do
        (authorisation-attempt/grant! (:authorisation/id authorisation))
        {:metadata {:current-user user}})
      {})))


(defmethod handle :deauthorise
  [_ _ _]

  "The current user will be deauthorised."

  {:metadata {:current-user nil}})


(defmethod handle :add-user
  [_ {:keys [user]} {:keys [current-user]}]

  "If the current user is an admin, and the given user doesn't
   already exist, then the given user will be created."

  ;; TODO - this needs to move into a proper spec
  (when-not (map? user)
    (throw (IllegalArgumentException. "Unsupported command parameters.")))

  (if (and (user-management/admin? current-user)
           (not (user-management/exists? (user/id (:user/email-address user)))))
    {:metadata
     {:id-resolution
      {(:user/id user) (user/create! (:user/email-address user)
                                     (:user/name user)
                                     (:user/roles user))}}}
    {}))


(defmethod handle :delete-user
  [_ {:keys [user/id]} {:keys [current-user]}]

  "If the current user is an admin, and user with the given user
   id exists then that user will be deleted."

  ;; TODO - this needs to move into a proper spec
  (when-not (uuid? id)
    (throw (IllegalArgumentException. "Unsupported command parameters.")))

  (if (and (user-management/admin? current-user)
           (user-management/exists? id))
    (do
      (user-management/delete! id)
      {})
    {}))


(defmethod handle :default
  [_ _ _]
  (throw (IllegalArgumentException. "Unsupported command method.")))
