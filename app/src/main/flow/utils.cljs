(ns flow.utils
  (:require [clojure.string :as string]))


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


(defn constrained-string?
  "Takes a number n and a string, and returns
   a boolean indication that the string has
   a positive length less than or equal to n."
  [n s]
  (and (string? s)
       (<= (count s) n)
       (not (string/blank? s))))


(defn constrain-string
  "Takes a number n and a string, and returns
   the string constrained to length n."
  [n s]
  (apply str (take n s)))


(defn sanitised-string?
  "Takes a string, and returns a boolean indication
   that the string has been sanitised. A string is
   considered sanitised if it has contains no whitespace,
   newline, or carriage return characters."
  [s]
  (and
   (string? s)
   (nil? (re-find #"\n|\r| " s))))


(defn sanitise-string
  "Takes a string, and santises it by removing whitespace,
   newlines, and carriage return characters."
  [s]
  (string/replace s #"\n|\r| " ""))


(defn email-address?
  "Takes a string and returns a boolean indication that
   the string adheres to an email address pattern."
  [s]
  (let [pattern #"[a-z0-9!#$%&'*+/=?^_`{|}~-]+(?:\.[a-z0-9!#$%&'*+/=?^_`{|}~-]+)*@(?:[a-z0-9](?:[a-z0-9-]*[a-z0-9])?\.)+[a-z0-9](?:[a-z0-9-]*[a-z0-9])?"]
    (boolean
     (and (string? s)
          (re-matches pattern s)))))
