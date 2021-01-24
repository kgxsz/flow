(ns flow.command
  (:require [ses-mailer.core :as mailer]))


(defmulti handle first)


(defmethod handle :example [command]
  {})


(defmethod handle :initialise-authorisation [command]
  (mailer/send-email {}
                     "noreply@flow.keigo.io"
                     "k.suzukawa@gmail.com"
                     "Hello Keigo"
                     {:html-body "<html>Authorisation code: 1234</html>"
                      :text-body "Authorisation code: 1234"})
  {})


(defmethod handle :finalise-authorisation [[_ {:keys [authorisation-code]}]]
  (if (= authorisation-code 1234)
    {:current-user-id 1101}
    {}))


(defmethod handle :deauthorise [command]
  {:current-user-id nil})


(defmethod handle :default [command]
  (throw (IllegalArgumentException. "Unsupported command method.")))
