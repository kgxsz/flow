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
