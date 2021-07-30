(ns flow.dev)


(defn repl []
  (shadow.cljs.devtools.api/repl :app))

;; - Secure the DB schema

;; - Think about flows, and their state transitions
;;   - Authorisation flow
;;   - Deauthorisation flow
