(ns flow.views.layouts.listings
  (:require [re-frame.core :as re-frame]
            [flow.views.layouts.cards.user :as cards.user]
            [flow.views.layouts.cards.authorisation :as cards.authorisation]
            [flow.views.processes.pagination :as pagination]
            [flow.utils :as u]))


(defn view [_
            {:keys [cards
                    pagination]}
            _]
  [:div
   {:class (u/bem [:cell :column])}
   [:div
    {:class (u/bem [:cell :column :align-start :padding-top-medium])}
    cards]
   [:div
    {:class (u/bem [:cell :padding-top-x-large])}
    pagination]])


(defn listings [{:keys [key entity-type] :as properties} views behaviours]
  (let [!ids (re-frame/subscribe [:listings/ids entity-type])]
    (fn [properties views behaviours]
      [view
       properties
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
        :pagination [pagination/pagination
                     {:key (concat key [:views :pagination])
                      :entity-type entity-type}
                     {}
                     {}]}
       {}])))
