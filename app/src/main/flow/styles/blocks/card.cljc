(ns flow.styles.blocks.card
  (:require [flow.styles.constants :as c]
            [flow.styles.utils :as u]
            [garden.def :refer [defstyles]]
            [garden.units :refer [px percent ms vh vw]]))


(defstyles card
  [:.card
   {:width (px (:xxx-huge c/filling))
    :background-color (:white-one c/colour)
    :border [[:solid (px (:xxx-tiny c/filling)) (:grey-three c/colour)]]
    :border-radius (px (:large c/radius))
    :padding (px (:medium c/spacing))}])
