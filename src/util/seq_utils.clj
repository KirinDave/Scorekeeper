(ns util.seq-utils
    (:use [clojure.contrib seq-utils]))

(defn index-of-obj 
  "Finds the index of an object, using the = test. If a permute function is given, 
   it is applied to the objects before comparison."
  ([seq object perm] (when-first [[index _] (filter #(= object (second %)) (indexed (map perm seq)))] index))
  ([seq object] (index-of-obj seq object identity)))

(defn vec-displace 
  "Inserts _object_ into vec at index, displacing objects to the right."
  [vec index object]
  (cond (zero? index)          (into [object] vec)
        (>= index (count vec)) (into vec [object])
        :else                  (-> (subvec vec 0 index) 
                                  (conj object) 
                                  (into (subvec vec index)))))
