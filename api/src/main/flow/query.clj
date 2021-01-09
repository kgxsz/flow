(ns flow.query)


(defmulti handle first)


(defmethod handle :example [[_ {:keys [example]}]]
  {:example example})


(defmethod handle :find [[_ _]]
  {:find 1})

(defmethod handle :default [query]
  (throw (IllegalArgumentException. "Unsupported query method.")))
