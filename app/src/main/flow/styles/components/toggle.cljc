(ns flow.styles.components.toggle
  (:require [flow.styles.constants :as c]
            [flow.styles.utils :as u]
            [garden.def :refer [defstyles]]
            [garden.units :refer [px percent ms vh vw]]))


(defstyles toggle
  [:.toggle
   [:&__body
    {:cursor :pointer
     :height (px (:medium c/filling))
     :width (px (:x-large c/filling))
     :background-color (:grey-one c/colour)
     :border-radius (px (:x-huge c/radius))}

    [:&--active
     {:background-color (:black-two c/colour)}]

    [:&__knob
     {:position :relative
      :top (px (:x-tiny c/spacing))
      :left (px (:x-tiny c/spacing))
      :height (px (:small c/filling))
      :width (px (:small c/filling))
      :background-color (:white-two c/colour)
      :border-radius (px (:x-huge c/radius))}

     [:&--active
      {:left (px (+ (:x-tiny c/spacing) (:medium c/spacing)))}]]]])
