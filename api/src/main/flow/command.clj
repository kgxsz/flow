(ns flow.command)


(defmulti handle first)


(defmethod handle :example [command]
  {})


(defmethod handle :authorise [command]
  {:current-user-id 3719})


(defmethod handle :deauthorise [command]
  {:current-user-id nil})


(defmethod handle :default [command]
  (throw (IllegalArgumentException. "Unsupported command method.")))
