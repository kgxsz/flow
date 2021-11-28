(ns flow.views.cards.authorisation
  (:require [re-frame.core :as re-frame]
            [flow.utils :as u]
            [cljs-time.coerce :as t.coerce]
            [cljs-time.format :as t.format]))


(defn view [{:keys [authorisation]}
            {:keys []}
            {:keys []}]
  [:div
   {:class (u/bem [:authorisation]
                  [:cell :column :align-start :padding-top-small])}
   [:div
    {:class (u/bem [:text :font-size-small :font-weight-bold :padding-left-tiny])}
    (str (:authorisation/id authorisation))]
   [:div
    {:class (u/bem [:text :font-size-x-small :padding-left-tiny])}
    (str (:authorisation/phrase authorisation))]
   [:div
    {:class (u/bem [:text :font-size-x-small :padding-left-tiny])}
    (str (:user/id authorisation))]
   [:div
    {:class (u/bem [:text :font-size-x-small :padding-left-tiny])}
    (->> (:authorisation/created-at authorisation)
         (t.coerce/from-date)
         (t.format/unparse (t.format/formatter "MMM dd, yyyy 'at' HH:mm.ss")))]
   [:div
    {:class (u/bem [:text :font-size-x-small :padding-left-tiny])}
    (or
     (some->> (:authorisation/granted-at authorisation)
              (t.coerce/from-date)
              (t.format/unparse (t.format/formatter "MMM dd, yyyy 'at' HH:mm.ss")))
     "n/a")]])


(defn card [{:keys [key id] :as properties} views behaviours]
  (let [!authorisation (re-frame/subscribe [:cards.authorisation/authorisation id])]
    (fn [properties views behaviours]
      [view
       (assoc properties
              :authorisation @!authorisation)
       {}
       {}])))
