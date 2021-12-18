(ns flow.views.layouts.cards.user
  (:require [re-frame.core :as re-frame]
            [flow.views.processes.user-deletion :as user-deletion]
            [flow.utils :as u]
            [flow.views.widgets.moment :as moment]))


(defn view [{:keys [user]}
            {:keys [created-moment
                    deleted-moment
                    user-deletion]}
            _]
  [:div
   {:class (u/bem [:card]
                  [:cell :column :align-start :margin-top-medium])}
   [:div
    {:class (u/bem [:text :font-size-xx-small :colour-black-four])}
    "Name"]
   [:div
    {:class (u/bem [:cell :width-cover]
                   [:text :font-size-x-large :padding-top-tiny])}
    (str (:user/name user))]
   [:div
    {:class (u/bem [:text :font-size-xx-small :colour-black-four :padding-top-medium])}
    "Email address"]
   [:div
    {:class (u/bem [:cell :width-cover]
                   [:text :font-size-x-large :padding-top-tiny])}
    (str (:user/email-address user))]
   [:div
    {:class (u/bem [:text :font-size-xx-small :colour-black-four :padding-top-medium])}
    "Roles"]

   [:div
    {:class (u/bem [:text :font-size-x-large])}
    (->> (:user/roles user)
         (map name)
         (interpose ", ")
         (apply str))]
   [:div
    {:class (u/bem [:text :font-size-xx-small :colour-black-four :padding-top-medium])}
    "Created"]
   [:div
    {:class (u/bem [:cell :padding-top-xx-tiny])}
    created-moment]

   [:div
    {:class (u/bem [:text :font-size-xx-small :colour-black-four :padding-top-medium])}
    "Deleted"]
   (if (:user/deleted-at user)
     [:div
      {:class (u/bem [:cell :padding-top-xx-tiny])}
      deleted-moment]
     [:div
      {:class (u/bem [:text :font-size-x-large :padding-top-tiny])}
      "n/a"]) 

   [:div
    {:class (u/bem [:cell :width-cover :height-xxx-tiny :margin-top-large :colour-grey-four])}]

   [:div
    {:class (u/bem [:cell :padding-top-tiny])}
    user-deletion]])


(defn card [{:keys [key id] :as properties} views behaviours]
  (let [!user (re-frame/subscribe [:cards.user/user id])]
    (fn [properties views behaviours]
      [view
       (assoc properties
              :user @!user)
       {:created-moment [moment/moment
                         {:value (:user/created-at @!user)}
                         {}
                         {}]
        :deleted-moment [moment/moment
                         {:value (:user/deleted-at @!user)}
                         {}
                         {}]
        :user-deletion [user-deletion/user-deletion
                        {:key (concat key [:views :user-deletion])
                         :id id}
                        {}
                        {}]}
       {}])))
