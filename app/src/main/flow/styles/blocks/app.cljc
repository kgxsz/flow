(ns flow.styles.blocks.app
  (:require [flow.styles.constants :as c]
            [flow.styles.utils :as u]
            [garden.def :refer [defstyles]]
            [garden.units :refer [px percent ms vh vw]]))


(defstyles app
  [:.app
   {}

   [:&__loader
    {:height (px (:tiny c/filling))
     :width (percent (:95 c/proportion))
     :background-color (:black-two c/colour)
     :position :fixed
     :animation [[:widen (ms 10000) :ease-out]]}]])
