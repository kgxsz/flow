(ns flow.query)


(defmulti handle first)


(defmethod handle :user [[_ {:keys [current-user-id]}]]
  (if current-user-id
    {:user {current-user-id {:id 1101
                             :name "Johnny McGee"
                             :email "johhny@mcgee.com"}}}
    {:user {}}))


(defmethod handle :default [query]
  (throw (IllegalArgumentException. "Unsupported query method.")))
