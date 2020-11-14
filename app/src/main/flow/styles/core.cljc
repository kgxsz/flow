(ns flow.styles.core
  (:require [flow.styles.constants :as c]
            [flow.styles.fonts :as fonts]
            [flow.styles.components.text :refer [text]]
            [flow.styles.components.icon :refer [icon]]
            [flow.styles.components.cell :refer [cell]]
            [flow.styles.components.page :refer [page]]
            [garden.def :refer [defstyles]]
            [garden.units :refer [px percent ms]]
            [normalize.core :refer [normalize]]))


(defstyles core

  ;; third party css
  [normalize]

  ;; foundations
  [:*
   {:box-sizing :border-box
    :list-style-type :none
    :margin 0
    :padding 0}]

  [:textarea
   {:outline :none
    :overflow :auto
    :-webkit-box-shadow :none
    :-moz-box-shadow :none
    :box-shadow :none
    :resize :none
    :padding (px (:xx-small c/spacing))
    :border [[:solid (px (:xxx-tiny c/filling)) (:white-three c/colour)]]
    :font-family  "Arial, \"Helvetica Neue\", Helvetica, sans-serif"
    :font-size (px (:medium c/font-size))
    :font-weight 400
    :line-height 1.3
    :font-variant :normal
    :text-transform :none
    :-webkit-font-smoothing :antialiased
    :-moz-osx-font-smoothing :grayscale
    :text-decoration :none}]

  ["textarea::placeholder"
   {:color (:grey-two c/colour)}]

  ;; fonts
  fonts/icomoon

  ;; components
  icon
  text
  cell
  page)
