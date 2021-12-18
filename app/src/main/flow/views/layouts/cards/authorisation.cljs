(ns flow.views.layouts.cards.authorisation
  (:require [re-frame.core :as re-frame]
            [flow.utils :as u]
            [flow.views.widgets.moment :as moment]))


(defn view [{:keys [authorisation]}
            {:keys [created-moment
                    granted-moment]}
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
    {:class (u/bem [:cell :padding-top-xx-tiny])}
    created-moment]
   [:div
    {:class (u/bem [:text :font-size-xx-small :colour-black-four :padding-top-medium])}
    "Granted"]
   (if (:authorisation/granted-at authorisation)
     [:div
      {:class (u/bem [:cell :padding-top-xx-tiny])}
      granted-moment]
     [:div
      {:class (u/bem [:text :font-size-x-large :padding-top-tiny])}
      "n/a"])])


(defn card [{:keys [key id] :as properties} views behaviours]
  (let [!authorisation (re-frame/subscribe [:cards.authorisation/authorisation id])]
    (fn [properties views behaviours]
      [view
       (assoc properties
              :authorisation @!authorisation)
       {:created-moment [moment/moment
                         {:value (:authorisation/created-at @!authorisation)}
                         {}
                         {}]
        :granted-moment [moment/moment
                         {:value (:authorisation/granted-at @!authorisation)}
                         {}
                         {}]}
       {}])))
