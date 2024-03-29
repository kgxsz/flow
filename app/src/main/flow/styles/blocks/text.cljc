(ns flow.styles.blocks.text
  (:require [flow.styles.constants :as c]
            [flow.styles.utils :as u]
            [garden.def :refer [defstyles]]
            [garden.units :refer [px percent ms vh vw]]))


(defstyles text
  [:.text {:font-family "\"Open Sans\", sans-serif"
           :font-size (px (:medium c/font-size))
           :font-weight 400
           :line-height 1.3
           :font-variant :normal
           :text-transform :none
           :-webkit-font-smoothing :antialiased
           :-moz-osx-font-smoothing :grayscale
           :text-decoration :none
           :word-wrap :break-word}

   [:&--padding-top
    (u/make-modifiers c/spacing :padding-top px)]

   [:&--padding-bottom
    (u/make-modifiers c/spacing :padding-bottom px)]

   [:&--padding-left
    (u/make-modifiers c/spacing :padding-left px)]

   [:&--padding-right
    (u/make-modifiers c/spacing :padding-right px)]

   [:&--margin-top
    (u/make-modifiers c/spacing :margin-top px)]

   [:&--margin-bottom
    (u/make-modifiers c/spacing :margin-bottom px)]

   [:&--font-size
    (u/make-modifiers c/font-size :font-size px)]

   [:&--colour
    (u/make-modifiers c/colour :color)]

   [:&--ellipsis {:display :block
                  :white-space :nowrap
                  :overflow :hidden
                  :text-overflow :ellipsis}]

   [:&--align-center {:text-align :center}]

   [:&--font-weight-bold {:font-weight 700}]])
