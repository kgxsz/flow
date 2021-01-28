(ns flow.styles.components.button
  (:require [flow.styles.constants :as c]
            [flow.styles.utils :as u]
            [garden.def :refer [defstyles]]
            [garden.units :refer [px percent ms vh vw]]))


;; TODO - fix all constants
(defstyles button
  [:.button
   {:cursor :pointer
    :height (px 40)
    :border [[:solid (px (:xx-tiny c/filling)) (:black-two c/colour)]]
    :border-radius (px (:medium c/radius))}

   [:&--disabled
    {:cursor :not-allowed
     :opacity (:10 c/fraction)}]

   [:&--primary
    {:color (:white-one c/colour)
     :background-color (:black-two c/colour)}]

   [:&--secondary
    {:color (:black-two c/colour)
     :background-color (:white-one c/colour)}]])
