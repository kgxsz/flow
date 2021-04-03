(ns flow.utils)


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
  "Takes a number and a string, and ensures
   that the string has been sanitised and
   has a length less than or equal to n.
   A string is considered sanitised if it
   has contains no whitespace, newline,
   or carriage return characters."
  [n s]
  (and
   (string? s)
   (nil? (re-find #"\n|\r| " s))
   (<= (count s) n)))


(defn email-address?
  "Takes a string and ensures that it
   adheres to an email address pattern."
  [s]
  (let [pattern #"[a-z0-9!#$%&'*+/=?^_`{|}~-]+(?:\.[a-z0-9!#$%&'*+/=?^_`{|}~-]+)*@(?:[a-z0-9](?:[a-z0-9-]*[a-z0-9])?\.)+[a-z0-9](?:[a-z0-9-]*[a-z0-9])?"]
    (boolean
     (and (string? s)
          (re-matches pattern s)))))
