(ns flow.views.entities.user
  (:require [re-frame.core :as re-frame]
            [flow.views.user-deletion :as user-deletion]
            [flow.views.button :as button]
            [flow.utils :as u]
            [cljs-time.coerce :as t.coerce]
            [cljs-time.format :as t.format]))


(defn view [{:keys [user]}
            {:keys [user-deletion]}
            _]
  [:div
   {:class (u/bem [:user]
                  [:cell :column :align-start :padding-top-small])}
   [:div
    {:class (u/bem [:text :font-size-small :font-weight-bold :padding-left-tiny])}
    (str (:user/id user))]
   [:div
    {:class (u/bem [:text :font-size-x-small :padding-left-tiny])}
    (str (:user/name user))]
   [:div
    {:class (u/bem [:text :font-size-x-small :padding-left-tiny])}
    (str (:user/email-address user))]
   [:div
    {:class (u/bem [:text :font-size-x-small :padding-left-tiny])}
    (->> (:user/created-at user)
         (t.coerce/from-date)
         (t.format/unparse (t.format/formatter "MMM dd, yyyy - HH:mm.ss")))]
   [:div
    {:class (u/bem [:text :font-size-x-small :padding-left-tiny])}
    (or
     (some->> (:user/deleted-at user)
              (t.coerce/from-date)
              (t.format/unparse (t.format/formatter "MMM dd, yyyy - HH:mm.ss")))
     "n/a")]
   [:div
    {:class (u/bem [:text :font-size-x-small :padding-left-tiny])}
    (->> (:user/roles user)
         (map name)
         (interpose ", ")
         (apply str))]
   user-deletion])


(defn user [{:keys [user/id] :as properties} views behaviours]
  (let [!user (re-frame/subscribe [:user/user id])]
    (fn [properties views behaviours]
      [view
       (assoc properties
              :user @!user)
       {:user-deletion [user-deletion/user-deletion
                        {:key [:views :app :views :pages.admin.users :views :user id :views :user-deletion]
                         :user/id id}
                        {}
                        {}]}
       {}])))
