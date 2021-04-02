(ns flow.utils
  (:require [clojure.string :as string]))


(defn index
  "Takes a key and map, uses the key to
   extract an index from the map, if it
   exists, returns a map with the index
   as key and the map as value, otherwise
   returns an empty map."
  [k m]
  (if-let [i (get m k)]
    {i m}
    {}))


(defn sanitised-string?
  "Takes a number n and a string s, and
   ensures that s is a string that has
   been sanitised and has a length less
   than or equal to n."
  [n s]
  (let [sanitise #(-> %
                      (string/trim)
                      (string/trim-newline)
                      (string/replace #" " ""))]
    (and
     (string? s)
     (= s (sanitise s))
     (<= (count s) n))))


(defn email-address?
  "Takes a string s and ensures that it
   is a string and adheres to an email
   address pattern."
  [s]
  (let [pattern #"[a-z0-9!#$%&'*+/=?^_`{|}~-]+(?:\.[a-z0-9!#$%&'*+/=?^_`{|}~-]+)*@(?:[a-z0-9](?:[a-z0-9-]*[a-z0-9])?\.)+[a-z0-9](?:[a-z0-9-]*[a-z0-9])?"]
    (and (string? s)
         (re-matches pattern s))))
