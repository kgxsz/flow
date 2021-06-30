(ns flow.command
  (:require [flow.entity.user :as user]
            [flow.entity.authorisation :as authorisation]
            [flow.email :as email]
            [flow.domain.user-management :as user-management]
            [flow.domain.authorisation-attempt :as authorisation-attempt]
            [flow.utils :as u]))


(defmulti handle (fn [method payload metadata session] method))


(defmethod handle :initialise-authorisation-attempt
  [_ {:keys [user/email-address]} _ _]
  "If the user with the given email address exists and has not been deleted,
   then a phrase will be generated, an authorisation will be created, and an
   email containing the phrase will be sent to the user such that they may
   finalise their authorisation attempt."
  (let [{:user/keys [id deleted-at] :as user} (user/fetch (user/id email-address))]
    (if (and (some? user) (nil? deleted-at))
      (let [phrase (authorisation-attempt/generate-phrase)]
        (authorisation/create! id phrase)
        (email/send-email! email-address (authorisation-attempt/email phrase))
        {})
      {})))


(defmethod handle :finalise-authorisation-attempt
  [_ {:keys [user/email-address authorisation/phrase]} _ _]
  "If a grantable authorisation is found to match the given email address and phrase,
   then the authorisation will be marked as granted, and a session will be created."
  (let [user (user/fetch (user/id email-address))
        authorisation (authorisation/fetch (authorisation/id (:user/id user) phrase))]
    (if (and (some? authorisation) (authorisation-attempt/grantable? authorisation))
      (do
        (authorisation/mutate!
         (:authorisation/id authorisation)
         authorisation-attempt/grant)
        {:session {:current-user user}})
      {})))


(defmethod handle :deauthorise
  [_ _ _ _]
  "The current user will be deauthorised."
  {:session {:current-user nil}})


(defmethod handle :add-user
  [_ {:user/keys [id email-address name roles]} _ {:keys [current-user]}]
  "If the current user is an admin, and the given user doesn't
   already exist, then the given user will be created."
  (if (and (user-management/admin? current-user)
           (nil? (user/fetch (user/id email-address))))
    {:metadata
     {:id-resolution
      {id (user/create! email-address name roles)}}}
    {}))


(defmethod handle :delete-user
  [_ {:keys [user/id]} _ {:keys [current-user]}]
  "If the current user is an admin, and the given user exists,
   then that user will be deleted."
  (if (and (user-management/admin? current-user)
           (user/fetch id))
    (do
      (user/mutate! id user-management/delete)
      {})
    {}))


(defmethod handle :default
  [method _ _ _]
  (u/report :internal-error (str "The command method" method " does not exist.")))
