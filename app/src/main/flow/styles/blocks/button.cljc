(ns flow.styles.blocks.button
  (:require [flow.styles.constants :as c]
            [flow.styles.utils :as u]
            [garden.def :refer [defstyles]]
            [garden.units :refer [px percent ms vh vw]]))


(defstyles button
  [:.button
   {:cursor :pointer
    :height (px (:x-large c/filling))
    :border [[:solid (px (:xx-tiny c/filling)) (:black-two c/colour)]]
    :border-radius (px (:medium c/radius))}

   [:&--primary
    {:color (:white-one c/colour)
     :background-color (:black-two c/colour)}]

   [:&--secondary
    {:color (:black-two c/colour)
     :background-color (:white-one c/colour)}]

   [:&--disabled
    {:cursor :default
     :opacity (:10 c/fraction)}]

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
     :width (px (:x-small c/filling))
     :height (px (:x-small c/filling))
     :margin-top (px (- (:xxx-small c/filling)))
     :margin-left (px (- (:xxx-small c/filling)))
     :border-radius (percent (:50 c/proportion))
     :border [[(px (:xxx-tiny c/filling)) :solid (:white-one c/colour)]]
     :border-top [[(px (:xxx-tiny c/filling)) :solid (:black-two c/colour)]]
     :animation [[:spin (ms 1000) :linear :infinite]]}]])
