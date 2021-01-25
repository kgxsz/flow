(ns flow.styles.components.input
  (:require [flow.styles.constants :as c]
            [flow.styles.utils :as u]
            [garden.def :refer [defstyles]]
            [garden.units :refer [px percent ms vh vw]]))


(defstyles input
  [:.input {:padding [[(px (:tiny c/spacing)) (px (:small c/spacing))]]
            :border [[:solid (px (:xxx-tiny c/filling)) (:white-three c/colour)]]
            :border-radius (px (:medium c/radius))
            :font-family "\"Open Sans\", sans-serif"
            :font-size (px (:medium c/font-size))
            :font-weight 400
            :line-height 1.3
            :font-variant :normal
            :text-transform :none}])
