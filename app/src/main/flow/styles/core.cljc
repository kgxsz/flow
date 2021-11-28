(ns flow.styles.core
  (:require [flow.styles.constants :as c]
            [flow.styles.fonts :as fonts]
            [flow.styles.animations :as animations]
            [flow.styles.blocks.text :refer [text]]
            [flow.styles.blocks.icon :refer [icon]]
            [flow.styles.blocks.cell :refer [cell]]
            [flow.styles.blocks.page :refer [page]]
            [flow.styles.blocks.input :refer [input]]
            [flow.styles.blocks.toggle :refer [toggle]]
            [flow.styles.blocks.link :refer [link]]
            [flow.styles.blocks.button :refer [button]]
            [flow.styles.blocks.app :refer [app]]
            [flow.styles.blocks.authorisation-attempt :refer [authorisation-attempt]]
            [flow.styles.blocks.user-addition :refer [user-addition]]
            [flow.styles.blocks.user :refer [user]]
            [flow.styles.blocks.authorisation :refer [authorisation]]
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

  ;; animations
  animations/spin
  animations/widen

  ;; components
  cell
  icon
  text
  input
  toggle
  link
  button
  app
  page
  authorisation-attempt
  user-addition
  user
  authorisation)
