(ns flow.views.home-page
  (:require [re-frame.core :as re-frame]
            [flow.utils :as u]))


(defn view [{:keys [authorised?]}
            _
            {:keys [initialise-authorisation
                    finalise-authorisation
                    deauthorise
                    update-route]}]
  [:div
   {:class (u/bem [:page])}
   [:div
    {:class (u/bem [:page__body])}
    [:div
     {:class (u/bem [:cell :column :padding-top-huge])}
     [:div
      {:class (u/bem [:icon :construction :font-size-xx-huge :align-center])}]
     [:div
      {:class (u/bem [:text :font-size-xx-huge :align-center])
       :on-click update-route}
      "Home"]
     (if authorised?
       [:div
        {:class (u/bem [:cell :row :padding-top-large])}
        [:div
         {:class (u/bem [:icon :arrow-right-circle :font-size-small])}]
        [:div
         {:class (u/bem [:text :font-size-large :padding-left-tiny])
          :on-click deauthorise}
         "Deauthorise"]]
       [:div
        [:div
         {:class (u/bem [:cell :row :padding-top-large])}
         [:div
          {:class (u/bem [:icon :arrow-right-circle :font-size-small])}]
         [:div
          {:class (u/bem [:text :font-size-large :padding-left-tiny])
           :on-click (partial initialise-authorisation {:email-address "k.suzukawa@gmail.com"})}
          "Initialise authorisation"]]
        [:div
         {:class (u/bem [:cell :row])}
         [:div
          {:class (u/bem [:icon :arrow-right-circle :font-size-small])}]
         [:div
          {:class (u/bem [:text :font-size-large :padding-left-tiny])
           :on-click (partial finalise-authorisation {:authorisation-code 1234})}
          "Finalise authorisation"]]])]]
   [:div
    {:class (u/bem [:page__footer])}]])


(defn home-page []
  (let [!authorised? (re-frame/subscribe [:authorised?])]
    (fn []
      [view
       {:authorised? @!authorised?}
       {}
       {:initialise-authorisation #(re-frame/dispatch [:initialise-authorisation %])
        :finalise-authorisation #(re-frame/dispatch [:finalise-authorisation %])
        :deauthorise #(re-frame/dispatch [:deauthorise])
        :update-route #(re-frame/dispatch [:update-route :admin])}])))
