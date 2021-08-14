(ns flow.styles.core
  (:require [flow.styles.constants :as c]
            [flow.styles.fonts :as fonts]
            [flow.styles.components.text :refer [text]]
            [flow.styles.components.icon :refer [icon]]
            [flow.styles.components.input :refer [input]]
            [flow.styles.components.toggle :refer [toggle]]
            [flow.styles.components.button :refer [button]]
            [flow.styles.components.cell :refer [cell]]
            [flow.styles.components.router :refer [router]]
            [flow.styles.components.page :refer [page]]
            [flow.styles.components.authorisation-attempt :refer [authorisation-attempt]]
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

  ["input[type=text],input[type=text]:disabled"
   {:outline :none
    :background-color (:white-one c/colour)
    :overflow :auto
    :-webkit-box-shadow :none
    :-moz-box-shadow :none
    :box-shadow :none
    :resize :none
    :appearance :none
    :-moz-appearance :none
    :-webkit-appearance :none
    :-webkit-font-smoothing :antialiased
    :-moz-osx-font-smoothing :grayscale
    :text-decoration :none}]

  ["input:-webkit-autofill"
   {:-webkit-box-shadow [["0" "0" "0px" "1000px" :white :inset]]}]

  ["input::placeholder"
   {:color (:grey-one c/colour)}]

  ;; fonts
  fonts/icomoon

  ;; components
  icon
  input
  toggle
  button
  text
  cell
  router
  page
  authorisation-attempt)
