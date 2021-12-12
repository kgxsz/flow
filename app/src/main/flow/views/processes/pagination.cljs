(ns flow.views.processes.pagination
  (:require [re-frame.core :as re-frame]
            [flow.views.layouts.cards.user :as cards.user]
            [flow.views.layouts.cards.authorisation :as cards.authorisation]
            [flow.views.widgets.button :as button]
            [flow.utils :as u]))


(defn view [{:keys [exhausted?]}
            {:keys [cards
                    button]}
            _]
  [:div
   {:class (u/bem [:pagination])}
   [:div
    {:class (u/bem [:cell :column :align-start :padding-top-medium])}
    cards]
   [:div
    {:class (u/bem [:cell :row :padding-top-large])}
    (if exhausted?
      [:div
       {:class (u/bem [:cell :row :height-large])}
       [:div
        {:class (u/bem [:icon :checkmark-circle :font-size-large])}]
       [:div
        {:class (u/bem [:text :padding-left-xx-small])}
        "All items loaded"]]
      button)]])


(defn pagination [{:keys [key entity-type] :as properties} views behaviours]
  (let [!ids (re-frame/subscribe [:pagination/ids key entity-type])
        !exhausted? (re-frame/subscribe [:pagination/exhausted? key])
        !pending? (re-frame/subscribe [:pagination/pending? key])]
    (fn [properties views behaviours]
      [view
       (assoc properties
              :exhausted? @!exhausted?)
       {:cards (for [id @!ids]
                 (case entity-type
                   :users
                   ^{:key id}
                   [cards.user/card
                    {:key (concat key [:views :cards.user id])
                     :id id
                     }
                    {}
                    {}]
                   :authorisations
                   ^{:key id}
                   [cards.authorisation/card
                    {:key (concat key [:views :cards.user id])
                     :id id}
                    {}
                    {}]))
        :button [button/button
                 {:type :tertiary
                  :label "Load more items"
                  :icon :arrow-down
                  :disabled? @!exhausted?
                  :pending? @!pending?}
                 {}
                 {:on-click #(re-frame/dispatch [:pagination/start key entity-type])}]}
       behaviours])))
