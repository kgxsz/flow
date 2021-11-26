(ns flow.access-control)


(defn select-accessible-queries
  "Selects the queries that are accessible either by default, or accessible
   due to one or more roles associated with the current user."
  [queries {:keys [user/roles]}]
  (let [default-accessible-queries [:current-user]
        role-accessible-queries {:admin [:current-user
                                         :users
                                         :user
                                         :authorisations]
                                 :customer [:current-user
                                            :users
                                            :user]}]
    (select-keys
     queries
     (cond-> default-accessible-queries
       (contains? roles :admin) (concat (:admin role-accessible-queries))
       (contains? roles :customer) (concat (:customer role-accessible-queries))))))


(defn select-accessible-commands
  "Selects the commands that are accessible either by default, or accessible
   due to one or more roles associated with the current user."
  [commands {:keys [user/roles]}]
  (let [default-accessible-commands [:initialise-authorisation-attempt
                                     :finalise-authorisation-attempt]
        role-accessible-commands {:admin [:initialise-authorisation-attempt
                                          :finalise-authorisation-attempt
                                          :deauthorise
                                          :add-user
                                          :delete-user]
                                  :customer [:initialise-authorisation-attempt
                                             :finalise-authorisation-attempt
                                             :deauthorise]}]
    (select-keys
     commands
     (cond-> default-accessible-commands
       (contains? roles :admin) (concat (:admin role-accessible-commands))
       (contains? roles :customer) (concat (:customer role-accessible-commands))))))


(defn select-accessible-user-keys
  "Selects the user entity's keys that are accessible either by default,
   or accessible due to the current user owning the entity, or accessible
   due to one or more roles associated with the current user."
  [user {:keys [user/id user/roles]}]
  (let [default-accessible-keys []
        owner-accessible-keys [:user/id
                               :user/email-address
                               :user/name
                               :user/roles
                               :user/created-at
                               :user/deleted-at]
        role-accessible-keys {:admin [:user/id
                                      :user/email-address
                                      :user/name
                                      :user/roles
                                      :user/created-at
                                      :user/deleted-at]
                              :customer [:user/id
                                         :user/name
                                         :user/created-at
                                         :user/deleted-at]}]
    (select-keys
     user
     (cond-> default-accessible-keys
       (= id (:user/id user)) (concat owner-accessible-keys)
       (contains? roles :admin) (concat (:admin role-accessible-keys))
       (contains? roles :customer) (concat (:customer role-accessible-keys))))))


(defn select-accessible-authorisation-keys
  "Selects the authorisation entity's keys that are accessible either by default,
   or accessible due to the current user owning the entity, or accessible due to
   one or more roles associated with the current user."
  [authorisation {:keys [user/id user/roles]}]
  (let [default-accessible-keys []
        owner-accessible-keys []
        role-accessible-keys {:admin [:authorisation/id
                                      :user/id
                                      :authorisation/phrase
                                      :authorisation/created-at
                                      :authorisation/granted-at]
                              :customer []}]
    (select-keys
     authorisation
     (cond-> default-accessible-keys
       (= id (:user/id authorisation)) (concat owner-accessible-keys)
       (contains? roles :admin) (concat (:admin role-accessible-keys))
       (contains? roles :customer) (concat (:customer role-accessible-keys))))))
