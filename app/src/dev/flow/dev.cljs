(ns flow.dev)


(defn ^:export db
  []
  (cljs.pprint/pprint
   (deref re-frame.db/app-db)))


(comment

  (db)

  )


;; Tasks:
;; - Address the repitition in page initialisation patterns
;;   - Every eage initialisation start event:
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

;; - Style authorisation card
;; - Style user card
