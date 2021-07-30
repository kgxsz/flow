(ns flow.dev)


(defn repl []
  (shadow.cljs.devtools.api/repl :app))

;; - Secure the DB schema
;; - Look into the success/error flows for API calls
;; - Look into multi-API calls

;; - Think about flows, their state transitions, and their affect on the DB
;;   - Authorisation
;;   - Deauthorisation
;;   - Load users
;;   - Load authorisations
;;   - Add a user
;;   - Delete a user
