(ns flow.styles.blocks.authorisation
  (:require [flow.styles.constants :as c]
            [flow.styles.utils :as u]
            [garden.def :refer [defstyles]]
            [garden.units :refer [px percent ms vh vw]]))


(defstyles authorisation
  [:.authorisation {:width (px (:xxx-huge c/filling))}])
