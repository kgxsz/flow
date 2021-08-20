(ns flow.views.pages.home-page
  (:require [re-frame.core :as re-frame]
            [flow.views.components.authorisation-attempt :as authorisation-attempt]
            [flow.views.components.deauthorisation :as deauthorisation]
            [flow.views.components.navigation :as navigation]
            [flow.utils :as u]))


(defn view [{:keys [status
                    authorised?]}
            {:keys [navigation
                    authorisation-attempt
                    deauthorisation]}
            _]
  [:div
   {:class (u/bem [:page])}
   [:div
    {:class (u/bem [:page__body])}
    [:div
     {:class (u/bem [:cell :column :padding-top-huge])}
     [:div
      {:class (u/bem [:icon :leaf :font-size-xxx-huge])}]
     [:div
      {:class (u/bem [:cell :padding-top-xx-large])}
      (case status

        :idle
        [:div
         {:class (u/bem [:text :align-center :padding-top-medium])}
         ;; TODO - deal with this more nicely
         "********************NOT REAAAADDYYYYe!!!!!!!!!!!!!!!!!!"]

        :initialisation-pending
        [:div
         {:class (u/bem [:text :align-center :padding-top-medium])}
         ;; TODO - deal with this more nicely
         "********************INITIALISING HOME PAGe!!!!!!!!!!!!!!!!!!"]

        :initialisation-successful
        (if authorised?
          [deauthorisation]
          [authorisation-attempt])

        :initialisation-error
        [:div
         {:class (u/bem [:text :align-center :padding-top-medium])}
         ;; TODO - deal with this more nicely
         "********************ERRRRRRORGe!!!!!!!!!!!!!!!!!!"])]]]
   [:div
    {:class (u/bem [:page__footer])}]])


(defn home-page []
  (let [!status (re-frame/subscribe [:home-page/status])
        !authorised? (re-frame/subscribe [:home-page/authorised?])]
    (fn []
      [view
       {:status @!status
        :authorised? @!authorised?}
       {:navigation navigation/navigation
        :authorisation-attempt authorisation-attempt/authorisation-attempt
        :deauthorisation deauthorisation/deauthorisation}
       {}])))
