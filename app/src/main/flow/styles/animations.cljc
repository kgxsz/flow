(ns flow.styles.animations
  (:require [garden.stylesheet :refer [at-keyframes]]))


(def spin
  (at-keyframes
   "spin"
   [:from
    {}]
   [:to
    {:transform "rotate(360deg)"}]))
