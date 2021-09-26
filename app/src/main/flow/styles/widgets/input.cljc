(ns flow.styles.widgets.input
  (:require [flow.styles.constants :as c]
            [flow.styles.utils :as u]
            [garden.def :refer [defstyles]]
            [garden.units :refer [px percent ms vh vw]]))


(defstyles input
  [:.input {}
   [:&__icon {:position :relative
              :top (px (:xx-small c/spacing))
              :left (px (:xxx-small c/spacing))
              :width (px (:medium c/filling))
              :padding-left (px (:xxx-small c/spacing))
              :padding-right (px (:xxx-small c/spacing))
              :background-color (:white-one c/colour)}]
   [:&__body {:width (percent (:100 c/proportion))
              :height (px (:x-large c/filling))
              :padding [[(px (:tiny c/spacing)) (px (:small c/spacing))]]
              :border [[:solid (px (:xxx-tiny c/filling)) (:white-three c/colour)]]
              :border-radius (px (:medium c/radius))
              :font-family "\"Open Sans\", sans-serif"
              :font-size (px (:medium c/font-size))
              :font-variant :normal
              :color (:black-two c/colour)
              :text-transform :none}]])
