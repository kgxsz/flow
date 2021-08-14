(ns flow.core
  (:require [reagent.dom :as reagent]
            [re-frame.core :as re-frame]
            [flow.effects :as effects]
            [flow.events :as events]
            [flow.subscriptions :as subscriptions]
            [flow.views.components.router :as router]))


(defn mount []
  (re-frame/clear-subscription-cache!)
  (reagent/render [router/router] (.getElementById js/document "container")))


(defn initialise []
  (re-frame/dispatch-sync [:router/initialisation-started])
  (mount))
