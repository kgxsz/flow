(ns flow.styles.components.page
  (:require [flow.styles.constants :as c]
            [flow.styles.utils :as u]
            [garden.def :refer [defstyles]]
            [garden.units :refer [px percent ms vh vw]]))


(defstyles page
  [:.page
   {:display :none
    :overflow :auto
    :background-color (:white-one c/colour)
    :color (:black-two c/colour)}

   (u/tiny-width
    {:display :none})

   (u/small-width
    {:display :block})

   (u/medium-width
    {:display :block})

   (u/large-width
    {:display :block})

   (u/huge-width
    {:display :block})

   [:&__body
    {:margin :auto
     :padding-left (-> c/spacing :medium px)
     :padding-right (-> c/spacing :medium px)
     :display :flex
     :flex-direction :column
     :align-items :center}

    (u/small-width
     {:width (-> c/breakpoint :small :start px)})

    (u/medium-width
     {:width (-> c/breakpoint :medium :start px)})

    (u/large-width
     {:width (-> c/breakpoint :large :start px)})

    (u/huge-width
     {:width (-> c/breakpoint :huge :start px)})]

   [:&__footer
    {:height (-> c/filling :xx-large px)}]])
