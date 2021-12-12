(ns flow.styles.blocks.user
  (:require [flow.styles.constants :as c]
            [flow.styles.utils :as u]
            [garden.def :refer [defstyles]]
            [garden.units :refer [px percent ms vh vw]]))


(defstyles user
  [:.user {:width (px (:xxx-huge c/filling))}])
