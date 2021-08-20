(ns flow.views.widgets.button
  (:require [re-frame.core :as re-frame]
            [flow.utils :as u]))


(defn view [{:keys [type
                    label
                    prefix-icon
                    icon
                    disabled?
                    pending?]}
            _
            {:keys [on-click]}]
  [:div
   ;; TODO - add some nice styling for when the button has the "pending" property
   {:class (u/bem [:button type (when disabled? :disabled) (when pending? :pending)]
                  [:cell :row :width-cover :height-x-large])
    :on-click (when-not (or disabled? pending?) on-click)}
   [:div
    {:class (u/bem [:text])}
    label]
   [:div
    {:class (u/bem [:icon icon :padding-left-xx-small])}]])


(defn button [properties behaviours]
  (let [!disabled? (re-frame/subscribe [(get-in properties [:subscriptions :disabled?])])
        !pending? (re-frame/subscribe [(get-in properties [:subscriptions :pending?])])]
    (fn [properties behaviours]
      [view
       (assoc properties
              :disabled? @!disabled?
              :pending? @!pending?)
       {}
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
