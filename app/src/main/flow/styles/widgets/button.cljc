(ns flow.styles.widgets.button
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

   [:&__icon-container
    {:width "30px"
     :height "30px"}]

   [:&__spinner
    {:position :relative}]

   [:&__spinner:before
    {:content "''"
     :will-change :animation
     :box-sizing :border-box
     :position :absolute
     :top "50%"
     :left "50%"
     :width "14px"
     :height "14px"
     :margin-top "-7px"
     :margin-left "-7px"
     :border-radius "50%"
     :border [["1px" "solid" (:white-one c/colour)]]
     :border-top [["1px" "solid" (:black-two c/colour)]]
     :animation [["spin" "1s" "linear" "infinite"]]}]])
