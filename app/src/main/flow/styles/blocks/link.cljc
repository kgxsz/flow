(ns flow.styles.blocks.link
  (:require [flow.styles.constants :as c]
            [flow.styles.utils :as u]
            [garden.def :refer [defstyles]]
            [garden.units :refer [px percent ms vh vw]]))


(defstyles link
  [:.link
   {:cursor :pointer
    :border-bottom [[:solid (px (:xx-tiny c/filling)) (:grey-four c/colour)]]}])
