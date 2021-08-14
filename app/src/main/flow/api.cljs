(ns flow.api
  (:require [re-frame.core :as re-frame]
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
  [parameters on-response on-error]
  (ajax/POST (make-url)
             {:params parameters
              :with-credentials true
              :handler on-response
              :error-handler on-error
              :response-format (ajax/transit-response-format {:handlers {"u" ->UUID "n" long}})}))
