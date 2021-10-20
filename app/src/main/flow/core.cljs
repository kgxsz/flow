(ns flow.core
  (:require [reagent.dom :as reagent]
            [re-frame.core :as re-frame]
            [flow.effects :as effects]
            [flow.events :as events]
            [flow.subscriptions :as subscriptions]
            [flow.router :as router]
            [flow.views.app :as app]))


(defn mount []
  (re-frame/clear-subscription-cache!)
  (reagent/render
   [app/app {} {} {}]
   (.getElementById js/document "container")))


(defn initialise []
  (router/initialise!)
  (router/start!)
  (re-frame/dispatch-sync [:app/initialise])
  (mount))
