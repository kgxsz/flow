(ns flow.views.widgets.button
  (:require [re-frame.core :as re-frame]
            [flow.utils :as u]))


(defn view [{:keys [type
                    label
                    prefix-icon
                    icon
                    disabled?
                    working?]}
            _
            {:keys [on-click]}]
  [:div
   ;; TODO - add some nice styling for when the button has the "working" property
   {:class (u/bem [:button type (when disabled? :disabled) (when working? :working)]
                  [:cell :row :width-cover :height-x-large])
    :on-click (when-not (or disabled? working?) on-click)}
   [:div
    {:class (u/bem [:text])}
    label]
   [:div
    {:class (u/bem [:icon icon :padding-left-xx-small])}]])


(defn button [properties behaviours]
  (let [!disabled? (re-frame/subscribe [(get-in properties [:subscriptions :disabled?])])
        !working? (re-frame/subscribe [(get-in properties [:subscriptions :working?])])]
    (fn [properties behaviours]
      [view
       (assoc properties
              :disabled? @!disabled?
              :working? @!working?)
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
