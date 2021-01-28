(ns flow.styles.components.input
  (:require [flow.styles.constants :as c]
            [flow.styles.utils :as u]
            [garden.def :refer [defstyles]]
            [garden.units :refer [px percent ms vh vw]]))


;; TODO - fix all constants
(defstyles input
  [:.input {}
   [:&__icon {:position :relative
              :top (px 8)
              :left (px 8)
              :width (px 26)
              :padding-left (px 6)
              :padding-right (px 6)
              :background-color :white}]
   [:&__body {:width (percent 100)
              :height (px 40)
              :padding [[(px (:tiny c/spacing)) (px (:small c/spacing))]]
              :border [[:solid (px (:xxx-tiny c/filling)) (:white-three c/colour)]]
              :border-radius (px (:medium c/radius))
              :font-family "\"Open Sans\", sans-serif"
              :font-size (px (:medium c/font-size))
              :font-weight 400
              :line-height 1.3
              :font-variant :normal
              :color (:black-two c/colour)
              :text-transform :none}]])
