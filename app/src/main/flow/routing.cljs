(ns flow.routing
  (:require [re-frame.core :as re-frame]
            [domkm.silk :as silk]))


(defonce !history (atom nil))


(def routes (silk/routes [[:home [[]]]
                          [:admin [["admin"]]]]))
