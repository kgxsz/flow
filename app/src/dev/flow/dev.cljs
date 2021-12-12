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
;;   - Every page initialisation start event:
;;     - Sets the router status
;;     - Specific page data is fetched
;;   - Every page initialisation end event:
;;     - All the routing information gets set
;;     - The page gets cleared down
;;     - Entities gets reset to what just got returned
;;     - Specific page and child view state is set up
;; - Shore up the DB specifications

;; - Style authorisation card
;; - Style user card
