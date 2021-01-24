(ns flow.command
  (:require [ses-mailer.core :as mailer]))


(defmulti handle first)


(defmethod handle :example [command]
  {})


(defmethod handle :initialise-authorisation [[_ {:keys [email-address]}]]
  (let [email-address-whitelist #{"k.suzukawa@gmail.com"}]
    (if (contains? email-address-whitelist email-address)
      (try
        (mailer/send-email {}
                           "Flow <noreply@flow.keigo.io>"
                           email-address
                           "Hello!"
                           {:html-body "<html>Authorisation code: 1234</html>"
                            :text-body "Authorisation code: 1234"})
        {}
        (catch Exception e
          {}))
      {})))


(defmethod handle :finalise-authorisation [[_ {:keys [authorisation-code]}]]
  (if (= authorisation-code 1234)
    {:current-user-id 1101}
    {}))


(defmethod handle :deauthorise [command]
  {:current-user-id nil})


(defmethod handle :default [command]
  (throw (IllegalArgumentException. "Unsupported command method.")))
