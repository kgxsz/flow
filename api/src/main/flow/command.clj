(ns flow.command)


(defmulti handle first)


(defmethod handle :example [command]
  {})


(defmethod handle :default [command]
  (throw (IllegalArgumentException. "Unsupported command method.")))
