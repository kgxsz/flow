(ns flow.dev
  (:require [devtools.core :as devtools]))


(defn repl []
  (shadow.cljs.devtools.api/repl :app))
