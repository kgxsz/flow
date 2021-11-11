(ns flow.dev)


(defn repl []
  (shadow.cljs.devtools.api/repl :app))

;; Tasks
;; - Do paging
;;   - Assumptions are made to simplify this all
;;   - Send limit and previous in metadata
;;   - Make it return the entities
;;   - Add the entities to the page, sorted by primary key
;; - Revisit all TODOs and document/prioritise the work needed
;; - Move entities into their own entity directories for cleanliness
;; - Secure the app DB schema
;; - Create a new lite button for tying up the sign out sync
;; - Style the user and authorisation cards nicely
;; - Revisit keying, and how things could be done differently with weaving.
;;   Develop a theory on this. Start with the auth attempt and user addition views/
;;   - Look into tear down and set up hooks?


;; Thoughts
;; - Keys can be broken into smaller constiuents and util functions can be used.
;; - Capitals in the names and emails aren't going down well. Fix this UX.


;; TODOs:
;; - Update the db/fetch-entites-test test
;; - Update the entity fetch all functions and their call sites to pass options through
;; - Update the API query site to get the information from metadata
;; - Update the specs
;; - Do the App work!
