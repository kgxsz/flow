(ns flow.views.navigation
  (:require [re-frame.core :as re-frame]
            [flow.utils :as u]))


(defn view [{:keys [current-user]}
            _
            {:keys [update-route
                    deauthorise]}]
  [:div
   {:class (u/bem [:cell :column])}
   [:div
    {:class (u/bem [:text :font-size-xx-huge])}
    (str "Hi " (:user/name current-user))]

   [:div
    {:class (u/bem [:cell :column :padding-top-large])}

    (when (contains? (:user/roles current-user) :admin)
      [:div
       {:class (u/bem [:cell :row :padding-top-small])}
       [:div
        {:class (u/bem [:icon :arrow-right-circle :font-size-small])}]
       [:div
        {:class (u/bem [:text :font-size-large :padding-left-tiny])
         :on-click update-route}
        "Go to admin page"]])
    [:div
     {:class (u/bem [:cell :row :padding-top-small])}
     [:div
      {:class (u/bem [:icon :arrow-right-circle :font-size-small])}]
     [:div
      {:class (u/bem [:text :font-size-large :padding-left-tiny])
       :on-click deauthorise}
      "Deauthorise"]]]])


(defn navigation []
  (let [!current-user (re-frame/subscribe [:current-user])]
    (fn []
      [view
       {:current-user @!current-user}
       {}
       {:update-route #(re-frame/dispatch [:update-route :admin])
        :deauthorise #(re-frame/dispatch [:deauthorise])}])))
