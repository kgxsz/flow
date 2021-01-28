(ns flow.styles.core
  (:require [flow.styles.constants :as c]
            [flow.styles.fonts :as fonts]
            [flow.styles.components.text :refer [text]]
            [flow.styles.components.icon :refer [icon]]
            [flow.styles.components.input :refer [input]]
            [flow.styles.components.button :refer [button]]
            [flow.styles.components.cell :refer [cell]]
            [flow.styles.components.page :refer [page]]
            [flow.styles.components.authorisation :refer [authorisation]]
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

  [:input
   {:outline :none
    :overflow :auto
    :-webkit-box-shadow :none
    :-moz-box-shadow :none
    :box-shadow :none
    :resize :none
    :-webkit-font-smoothing :antialiased
    :-moz-osx-font-smoothing :grayscale
    :text-decoration :none}]

  ["input::placeholder"
   {:color (:grey-one c/colour)}]

  ;; fonts
  fonts/icomoon

  ;; components
  icon
  input
  button
  text
  cell
  page
  authorisation)
