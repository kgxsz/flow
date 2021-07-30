(ns flow.views.widgets.input
  (:require [re-frame.core :as re-frame]
            [flow.utils :as u]))


(defn view [{:keys [icon
                    value
                    placeholder]}
            _
            {:keys [on-change]}]
  [:div
   {:class (u/bem [:input])}
   [:div
    {:class (u/bem [:input__icon]
                   [:icon icon :font-size-large :colour-black-four])}]
   [:input
    {:class (u/bem [:input__body])
     :type :text
     :value value
     :placeholder placeholder
     :on-change #(on-change (.. % -target -value))}]])


(defn input [properties behaviours]
  (let [!value (re-frame/subscribe [(get-in properties [:subscriptions :value])])]
    (fn [properties behaviours]
      [view
       (assoc properties
              :value @!value)
       {}
       behaviours])))
