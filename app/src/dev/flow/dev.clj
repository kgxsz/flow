(ns flow.dev)


(defn repl []
  (shadow.cljs.devtools.api/repl :app))

;; Tasks:
;; - Style authorisation card
;; - Style user card
;; - Explore flattening the DB a litte to give pages firs class citizenship?
;;   - Extract routing, separate events, call globally
;;     - Request routing
;;     - Start routing
;;     - End routing
;;   - Extract session
;;   - Extract pages
;; - Address the repitition in page initialisation patterns
;;   - Every page initialisation start event:
;;     - Sets the app status to :routing (why? only the router component in app cares)
;;     - Specific page and child view data is fetched
;;   - Every page initialisation end event:
;;     - Sets the app status to :idle (why? nobody uses it)
;;     - All the routing information gets set
;;     - Session gets set
;;     - Entities gets reset to what just got returned
;;     - All views get reset
;;     - Specific page and child view state is set up
;; - Shore up the DB specifications
