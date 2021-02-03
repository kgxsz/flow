(ns flow.command
  (:require [flow.domain.authorisation :as authorisation]))


(defmulti handle first)


(defmethod handle :initialise-authorisation [[_ {:keys [email-address]}]]
  (try
    (authorisation/send-authorisation-email email-address)
    {}
    (catch Exception e
      {})))


(defmethod handle :finalise-authorisation [[_ {:keys [phrase]}]]
  (if (= phrase "banana-hat-cup")
    {:current-user-id 1101}
    {}))


(defmethod handle :deauthorise [command]
  {:current-user-id nil})


(defmethod handle :default [command]
  (throw (IllegalArgumentException. "Unsupported command method.")))
