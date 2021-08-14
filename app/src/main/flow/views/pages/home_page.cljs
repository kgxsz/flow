(ns flow.views.pages.home-page
  (:require [re-frame.core :as re-frame]
            [flow.views.components.authorisation-attempt :as authorisation-attempt]
            [flow.views.components.navigation :as navigation]
            [flow.utils :as u]))


(defn view [{:keys [status
                    authorised?]}
            {:keys [navigation
                    authorisation-attempt]}
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

        :uninitialised
        [:div
         {:class (u/bem [:text :align-center :padding-top-medium])}
         ;; TODO - deal with this more nicely
         "********************NOT REAAAADDYYYYe!!!!!!!!!!!!!!!!!!"]

        :initialising
        [:div
         {:class (u/bem [:text :align-center :padding-top-medium])}
         ;; TODO - deal with this more nicely
         "********************INITIALISING HOME PAGe!!!!!!!!!!!!!!!!!!"]

        :initialised
        (if authorised?
          [:div
           {:class (u/bem [:text :align-center :padding-top-medium])}

           ;; TODO - bring navigation in eventually
           "********************AUTHOORSED"]
          #_[navigation]
          [authorisation-attempt])

        :initialisation-errored
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
        :authorisation-attempt authorisation-attempt/authorisation-attempt}
       {}])))
