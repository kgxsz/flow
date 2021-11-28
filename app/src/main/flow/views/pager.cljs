(ns flow.views.pager
  (:require [re-frame.core :as re-frame]
            [flow.views.button :as button]
            [flow.utils :as u]))


(defn view [{:keys [exhausted?]}
            {:keys [button]}
            _]
  [:div
   {:class (u/bem [:pager])}
   (if exhausted?
     [:div
      {:class (u/bem [:cell :row])}
      [:div
       {:class (u/bem [:icon :checkmark-circle :font-size-large])}]
      [:div
       {:class (u/bem [:text :font-size-small :padding-left-xx-small])}
       "All items loaded"]]
     button)])


(defn pager [properties views behaviours]
  [view
   properties
   {:button [button/button
             {:type :tertiary
              :label "Load more items"
              :icon :arrow-down
              :disabled? false
              :pending? (:pending? properties)}
             {}
             {:on-click (:on-click behaviours)}]}
   behaviours])
