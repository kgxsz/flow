(ns flow.views.button
  (:require [re-frame.core :as re-frame]
            [flow.utils :as u]))


(defn view [{:keys [type
                    disabled?
                    label
                    prefix-icon
                    icon]}
            {:keys [on-click]}]
  [:div
   {:class (u/bem [:button type (when disabled? :disabled)]
                  [:cell :row :width-cover :height-x-large])
    :on-click (when-not disabled? on-click)}
   [:div
    {:class (u/bem [:text])}
    label]
   [:div
    {:class (u/bem [:icon icon :padding-left-xx-small])}]])



(defn button [properties behaviours]
  (let [!disabled? (re-frame/subscribe [(get-in properties [:subscriptions :disabled?])])]
    (fn [properties behaviours]
      [view
       (assoc properties
              :disabled? @!disabled?)
       behaviours])))


(defn primary-button [properties behaviours]
  [button
   (assoc properties
          :type :primary)
   behaviours])


(defn secondary-button [properties behaviours]
  [button
   (assoc properties
          :type :secondary)
   behaviours])
