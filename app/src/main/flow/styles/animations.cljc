(ns flow.styles.animations
  (:require [garden.stylesheet :refer [at-keyframes]]
            [garden.units :refer [px percent ms vh vw]]))


(def spin
  (at-keyframes
   "spin"
   [:from
    {}]
   [:to
    {:transform "rotate(360deg)"}]))


(def widen
  (at-keyframes
   "widen"
   ["0%"
    {:width (percent 0)}]
   ["10%"
    {:width (percent 90)}]
   ["100%"
    {:width (percent 95)}]))
