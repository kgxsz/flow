(ns flow.styles.components.deauthorisation
  (:require [flow.styles.constants :as c]
            [flow.styles.utils :as u]
            [garden.def :refer [defstyles]]
            [garden.units :refer [px percent ms vh vw]]))


(defstyles deauthorisation
  [:.deauthorisation {:width (px (:xxx-huge c/filling))}])
