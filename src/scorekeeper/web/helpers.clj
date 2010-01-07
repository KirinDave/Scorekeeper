(ns scorekeeper.web.helpers
    (:require [net.cgrand.enlive-html :as en]
              [compojure])
    (:import [java.io.File]))

; The server can do so much, for so many leagues.
(defonce leagues (atom {"default" *league*}))

(defn get-league! 
  ([name] (if-let [l (@leagues name)]
            l                 ; We already have a league for this name
            (let [nl (new-league name)] 
              (swap! leagues assoc name nl)
              nl)))
  ([] (get-league! "default")))


(defmacro RESOURCE 
  "For use in routes* macros, the extension is placed into regexp which is used
  to grab files that end that way and to map into ./site/_mapping_."
  [extension mapping]
  (let [pattern (re-pattern (str "/(.+\\." extension ")$"))
        loc     (str "./site/" mapping)]
    `(compojure/GET ~pattern (or (java.io.File. ~loc ((:route-params ~'request) 0))
                       (compojure/page-not-found))))) 
