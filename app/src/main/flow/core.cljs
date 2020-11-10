(ns flow.core
  (:require [reagent.dom :as reagent]
            [re-frame.core :as re-frame]
            [flow.effects :as effects]
            [flow.events :as events]
            [flow.subscriptions :as subscriptions]
            [flow.views.core :as views.core]))


(defn mount []
  (re-frame/clear-subscription-cache!)
  (reagent/render [views.core/core] (.getElementById js/document "container")))


(defn initialise []
  (re-frame/dispatch-sync [:initialise])
  (mount))
