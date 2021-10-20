(ns flow.views.input
  (:require [re-frame.core :as re-frame]
            [flow.utils :as u]))


(defn view [{:keys [icon
                    value
                    placeholder
                    disabled?]}
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
     :disabled disabled?
     :on-change #(on-change (.. % -target -value))}]])


(defn input [properties views behaviours]
  (let [{:keys [key]} properties
        !value (re-frame/subscribe [:input/value key])]
    (fn [properties views behaviours]
      [view
       (assoc properties :value @!value)
       {}
       {:on-change #(re-frame/dispatch [:input/update key %])}
       behaviours])))
