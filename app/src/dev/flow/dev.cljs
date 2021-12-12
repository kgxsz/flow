(ns flow.dev)


(defn ^:export db
  []
  (cljs.pprint/pprint
   (deref re-frame.db/app-db)))


(comment

  (db)

  )

;; Tasks:
;; - Style authorisation card
;;   - Decide what properties to expose
;; - Style user card
;;   - Decide what properties to expose
