(ns flow.views.widgets.moment
  (:require [re-frame.core :as re-frame]
            [flow.utils :as u]
            [cljs-time.coerce :as t.coerce]
            [cljs-time.format :as t.format]))


(defn view [{:keys [value]}
            _
            _]
  [:div
   {:class (u/bem [:cell :column :align-start])}
   [:div
    {:class (u/bem [:cell :row :align-end :height-medium])}
    [:div
     {:class (u/bem [:text :font-size-x-large])}
     (->> value
          (t.coerce/from-date)
          (t.format/unparse (t.format/formatter "MMM dd")))]
    [:div
     {:class (u/bem [:text :font-size-xx-small :padding-left-x-tiny :padding-bottom-xx-tiny])}
     (->> value
          (t.coerce/from-date)
          (t.format/unparse (t.format/formatter "/ yyyy")))]]
   [:div
    {:class (u/bem [:cell :row :align-end :height-medium])}
    [:div
     {:class (u/bem [:text :font-size-xx-large])}
     (->> value
          (t.coerce/from-date)
          (t.format/unparse (t.format/formatter "HH:mm")))]
    [:div
     {:class (u/bem [:text :font-size-xx-small :padding-bottom-xx-tiny])}
     (->> value
          (t.coerce/from-date)
          (t.format/unparse (t.format/formatter ".ss")))]]])


(defn moment [properties views behaviours]
  [view
   properties
   views
   behaviours])
