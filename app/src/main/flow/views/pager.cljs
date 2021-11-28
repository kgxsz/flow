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


(defn pager [{:keys [key entity] :as properties} views behaviours]
  (let [!exhausted? (re-frame/subscribe [:pager/exhausted? key])
        !pending? (re-frame/subscribe [:pager/pending? key])]
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
                 {:on-click #(re-frame/dispatch [:pager/start key entity])}]}
       behaviours])))
