(ns flow.styles.widgets.link
  (:require [flow.styles.constants :as c]
            [flow.styles.utils :as u]
            [garden.def :refer [defstyles]]
            [garden.units :refer [px percent ms vh vw]]))


(defstyles link
  [:.link
   {:cursor :pointer
    :padding-right "6px"}

   [:&--pending
    {:cursor :default}]

   [:&__spinner
    {:position :relative}]

   [:&__spinner:before
    {:content "''"
     :will-change :animation
     :box-sizing :border-box
     :position :absolute
     :top (percent (:50 c/proportion))
     :left (percent (:50 c/proportion))
     :width (px (:xx-small c/filling))
     :height (px (:xx-small c/filling))
     :margin-top (px (- (:tiny c/filling)))
     :margin-left (px (- (:tiny c/filling)))
     :border-radius (percent (:50 c/proportion))
     :border [[(px (:xxx-tiny c/filling)) :solid (:white-one c/colour)]]
     :border-top [[(px (:xxx-tiny c/filling)) :solid (:black-two c/colour)]]
     :animation [[:spin (ms 1000) :linear :infinite]]}]])
