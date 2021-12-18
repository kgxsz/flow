(ns flow.views.layouts.cards.authorisation
  (:require [re-frame.core :as re-frame]
            [flow.utils :as u]
            [cljs-time.coerce :as t.coerce]
            [cljs-time.format :as t.format]))


(defn view [{:keys [authorisation]}
            _
            _]
  [:div
   {:class (u/bem [:card]
                  [:cell :column :align-start :margin-top-medium])}
   [:div
    {:class (u/bem [:text :font-size-xx-small :colour-black-four])}
    "Phrase"]
   [:div
    {:class (u/bem [:cell :width-cover]
                   [:text :font-size-x-large :padding-top-tiny])}
    (str (:authorisation/phrase authorisation))]

   [:div
    {:class (u/bem [:text :font-size-xx-small :colour-black-four :padding-top-medium])}
    "Created"]

   [:div
    {:class (u/bem [:cell :column :align-start :padding-top-xx-tiny])}
    [:div
     {:class (u/bem [:cell :row :align-end :height-medium])}
     [:div
      {:class (u/bem [:text :font-size-x-large])}
      (->> (:authorisation/created-at authorisation)
           (t.coerce/from-date)
           (t.format/unparse (t.format/formatter "MMM dd")))]
     [:div
      {:class (u/bem [:text :font-size-xx-small :padding-left-x-tiny :padding-bottom-xx-tiny])}
      (->> (:authorisation/created-at authorisation)
           (t.coerce/from-date)
           (t.format/unparse (t.format/formatter "/ yyyy")))]]
    [:div
     {:class (u/bem [:cell :row :align-end :height-medium])}
     [:div
      {:class (u/bem [:text :font-size-xx-large])}
      (->> (:authorisation/created-at authorisation)
           (t.coerce/from-date)
           (t.format/unparse (t.format/formatter "HH:mm")))]
     [:div
      {:class (u/bem [:text :font-size-xx-small :padding-bottom-xx-tiny])}
      (->> (:authorisation/created-at authorisation)
           (t.coerce/from-date)
           (t.format/unparse (t.format/formatter ".ss")))]]]


   [:div
    {:class (u/bem [:text :font-size-xx-small :colour-black-four :padding-top-medium])}
    "Granted"]

   (if (:authorisation/granted-at authorisation)
     [:div
      {:class (u/bem [:cell :column :align-start :padding-top-xx-tiny])}
      [:div
       {:class (u/bem [:cell :row :align-end :height-medium])}
       [:div
        {:class (u/bem [:text :font-size-x-large])}
        (->> (:authorisation/granted-at authorisation)
             (t.coerce/from-date)
             (t.format/unparse (t.format/formatter "MMM dd")))]
       [:div
        {:class (u/bem [:text :font-size-xx-small :padding-left-x-tiny :padding-bottom-xx-tiny])}
        (->> (:authorisation/granted-at authorisation)
             (t.coerce/from-date)
             (t.format/unparse (t.format/formatter "/ yyyy")))]]
      [:div
       {:class (u/bem [:cell :row :align-end :height-medium])}
       [:div
        {:class (u/bem [:text :font-size-xx-large])}
        (->> (:authorisation/granted-at authorisation)
             (t.coerce/from-date)
             (t.format/unparse (t.format/formatter "HH:mm")))]
       [:div
        {:class (u/bem [:text :font-size-xx-small :padding-bottom-xx-tiny])}
        (->> (:authorisation/granted-at authorisation)
             (t.coerce/from-date)
             (t.format/unparse (t.format/formatter ".ss")))]]]

     [:div
      {:class (u/bem [:text :font-size-x-large :padding-top-tiny])}
      "n/a"])])


(defn card [{:keys [key id] :as properties} views behaviours]
  (let [!authorisation (re-frame/subscribe [:cards.authorisation/authorisation id])]
    (fn [properties views behaviours]
      [view
       (assoc properties
              :authorisation @!authorisation)
       {}
       {}])))
