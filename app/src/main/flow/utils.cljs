(ns flow.utils
  (:require [cemerick.url :as url]))


(defn bem
  "Creates a class string from bem structured arguments. Take multiple arguments in vectors.
  Each vector is composed of the block-elements keyword, then the optional modifiers.
  (bem [:block__element__element :modifier :modifier]
       [:block__element__element :modifier (if pred? :modifier-a :modifier-b) (when pred? :modifier-a)])"
  [& xs]
  (->> (for [x xs]
         (let [block-elements (first x)
               modifiers (->> x rest (remove nil?))]
           (cons
            (name block-elements)
            (for [modifier modifiers]
              (str (name block-elements) "--" (name modifier))))))
       (flatten)
       (interpose " ")
       (apply str)))


(defn make-url
  "Makes the API url by looking at the current host, and adding the path."
  [path]
  (let [{:keys [protocol host]} (url/url (.. js/window -location -href))]
    (-> (url/url "")
        (assoc :protocol protocol)
        (assoc :host (str "api." host))
        (assoc :path (->> path name (str "/")))
        (str))))


(defn valid-email-address?
  [email-address]
  (let [pattern #"[a-z0-9!#$%&'*+/=?^_`{|}~-]+(?:\.[a-z0-9!#$%&'*+/=?^_`{|}~-]+)*@(?:[a-z0-9](?:[a-z0-9-]*[a-z0-9])?\.)+[a-z0-9](?:[a-z0-9-]*[a-z0-9])?"]
    (and (string? email-address)
         (re-matches pattern email-address))))
