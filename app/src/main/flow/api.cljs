(ns flow.api
  (:require [re-frame.core :as re-frame]
            [cljs.core.async :refer [<!]]
            [cljs.core.async :refer-macros [go]]
            [cemerick.url :as url]
            [ajax.core :as ajax]))


(defn make-url
  "Makes the API url by looking at the current protocol and host and prefixing 'api'."
  []
  (let [{:keys [protocol host]} (url/url (.. js/window -location -href))]
    (-> (url/url "")
        (assoc :protocol protocol)
        (assoc :host (str "api." host))
        (str))))


(defn request!
  [parameters on-response on-error delay]
  (ajax/POST (make-url)
             {:params parameters
              :with-credentials true
              :handler #(go (<! delay) (re-frame/dispatch (into on-response [%])))
              :error-handler #(go (<! delay) (re-frame/dispatch (into on-error [%])))
              :response-format (ajax/transit-response-format {:handlers {"u" ->UUID "n" long}})}))
