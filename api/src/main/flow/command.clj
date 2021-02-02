(ns flow.command
  (:require [flow.authorisation :as authorisation]))


(defmulti handle first)


(defmethod handle :example [command]
  {})


(defmethod handle :initialise-authorisation [[_ {:keys [email-address]}]]
  (let [magic-phrase authorisation/generate-magic-phrase]
    (if authorisation/whitelisted-email-address?
      (try
        (authorisation/send-authorisation-email)
        {}
        (catch Exception e
          {}))
      {})))


(defmethod handle :finalise-authorisation [[_ {:keys [phrase]}]]
  (if (= phrase "banana-hat-cup")
    {:current-user-id 1101}
    {}))


(defmethod handle :deauthorise [command]
  {:current-user-id nil})


(defmethod handle :default [command]
  (throw (IllegalArgumentException. "Unsupported command method.")))
