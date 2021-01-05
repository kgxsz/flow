(ns flow.query)


(defmulti handle first)


(defmethod handle :example [[_ {:keys [example]}]]
  {:example example})


(defmethod handle :default [query]
  (throw (IllegalArgumentException. "Unsupported query method.")))
