(ns flow.dev)


(defn ^:export db
  []
  (cljs.pprint/pprint
   (deref re-frame.db/app-db)))


(comment

  (db)

  )
