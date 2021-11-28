(ns flow.views.processes.pagination
  (:require [re-frame.core :as re-frame]
            [flow.views.widgets.button :as button]
            [flow.utils :as u]))


(defn view [{:keys [exhausted?]}
            {:keys [button]}
            _]
  [:div
   {:class (u/bem [:pagination])}
   (if exhausted?
     [:div
      {:class (u/bem [:cell :row :height-large])}
      [:div
       {:class (u/bem [:icon :checkmark-circle :font-size-large])}]
      [:div
       {:class (u/bem [:text :padding-left-xx-small])}
       "All items loaded"]]
     button)])


(defn pagination [{:keys [key entity-type] :as properties} views behaviours]
  (let [!exhausted? (re-frame/subscribe [:pagination/exhausted? key])
        !pending? (re-frame/subscribe [:pagination/pending? key])]
    (fn [properties views behaviours]
      [view
       (assoc properties
              :exhausted? @!exhausted?)
       {:button [button/button
                 {:type :tertiary
                  :label "Load more items"
                  :icon :arrow-down
                  :disabled? @!exhausted?
                  :pending? @!pending?}
                 {}
                 {:on-click #(re-frame/dispatch [:pagination/start key entity-type])}]}
       behaviours])))
