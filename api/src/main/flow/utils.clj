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
