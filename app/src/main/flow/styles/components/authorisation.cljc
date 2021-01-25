(ns flow.styles.components.authorisation
  (:require [flow.styles.constants :as c]
            [flow.styles.utils :as u]
            [garden.def :refer [defstyles]]
            [garden.units :refer [px percent ms vh vw]]))


(defstyles authorisation
  [:.authorisation {:width (px 270)}
   [:&__email-address {}
    [:&__icon {:position :relative
               :top (px 8)
               :left (px 8)
               :width (px 26)
               :padding-left (px 6)
               :padding-right (px 6)
               :background-color :white}]
    [:&__input {:width (percent 100)}]
    [:&__button {:width (percent 100)
                 :border-radius (px 3)
                 :height (px 40)
                 :background-color "#EEE"}]]])
