(ns flow.utils
  (:require [clojure.string :as string]
            [clojure.spec.alpha :as s]
            [expound.alpha :as expound]))


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


(defn constrained-string?
  "Takes a number and a string, and returns
   a boolean indication that the string has
   a positive length less than or equal to n."
  [n s]
  (and (string? s)
       (<= (count s) n)
       (not (string/blank? s))))


(defn sanitised-string?
  "Takes a string, and returns a boolean indication
   that the string has been sanitised. A string is
   considered sanitised if it has contains no whitespace,
   newline, or carriage return characters."
  [s]
  (and
   (string? s)
   (nil? (re-find #"\n|\r| " s))))


(defn email-address?
  "Takes a string and returns a boolean indication that
   the string adheres to an email address pattern."
  [s]
  (let [pattern #"[a-z0-9!#$%&'*+/=?^_`{|}~-]+(?:\.[a-z0-9!#$%&'*+/=?^_`{|}~-]+)*@(?:[a-z0-9](?:[a-z0-9-]*[a-z0-9])?\.)+[a-z0-9](?:[a-z0-9-]*[a-z0-9])?"]
    (boolean
     (and (string? s)
          (re-matches pattern s)))))


(defn validate
  "Takes a specification and data, and returns the data if it does not
   violate the specification, if it does, then an illegal state exception
   is thrown and the violaton is printed out. This function should only
   be used for internal violations, not for validating external inputs."
  [specification data]
  (if (s/valid? specification data)
    data
    (do
      (expound/expound specification data)
      (throw (IllegalStateException. "Specification violation")))))
