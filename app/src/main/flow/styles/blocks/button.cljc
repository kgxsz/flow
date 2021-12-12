(ns flow.styles.blocks.button
  (:require [flow.styles.constants :as c]
            [flow.styles.utils :as u]
            [garden.def :refer [defstyles]]
            [garden.units :refer [px percent ms vh vw]]))


(defstyles button
  [:.button
   {:cursor :pointer
    :padding-left (px (:large c/spacing))
    :padding-right (px (:xx-small c/spacing))}

   [:&--primary
    {:background-color (:black-two c/colour)
     :height (px (:x-large c/filling))
     :width (percent (:100 c/proportion))
     :border [[:solid (px (:xx-tiny c/filling)) (:black-two c/colour)]]
     :border-radius (px (:medium c/radius))}]

   [:&--secondary
    {:background-color (:white-one c/colour)
     :height (px (:x-large c/filling))
     :width (percent (:100 c/proportion))
     :border [[:solid (px (:xx-tiny c/filling)) (:black-two c/colour)]]
     :border-radius (px (:medium c/radius))}]

   [:&--tertiary
    {:padding-left (px 0)
     :padding-right (px (:small c/spacing))}]

   [:&--disabled
    {:cursor :default
     :opacity (:10 c/fraction)}]

   [:&--pending
    {:cursor :default}]

   [:&__label

    [:&--primary
     {:color (:white-one c/colour)}]

    [:&--secondary
     {:color (:black-two c/colour)}]

    [:&--tertiary
     {:color (:black-two c/colour)}]]

   [:&__icon

    [:&--primary
     {:color (:white-one c/colour)}]

    [:&--secondary
     {:color (:black-two c/colour)}]

    [:&--tertiary
     {:color (:black-two c/colour)}]]

   [:&__spinner
    {:position :relative}

    [:&:before
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
      :animation [[:spin (ms 1000) :linear :infinite]]}]

    [:&--primary:before
     {:border [[(px (:xxx-tiny c/filling)) :solid (:white-one c/colour)]]
      :border-top [[(px (:xxx-tiny c/filling)) :solid (:black-two c/colour)]]}]

    [:&--secondary:before
     {:border [[(px (:xxx-tiny c/filling)) :solid (:black-two c/colour)]]
      :border-top [[(px (:xxx-tiny c/filling)) :solid (:white-one c/colour)]]}]

    [:&--tertiary:before
     {:border [[(px (:xxx-tiny c/filling)) :solid (:black-two c/colour)]]
      :border-top [[(px (:xxx-tiny c/filling)) :solid (:white-one c/colour)]]}]]])
