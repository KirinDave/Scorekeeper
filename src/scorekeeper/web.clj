(ns scorekeeper.web 
    (:use [scorekeeper.game]
          [clojure.contrib str-utils seq-utils duck-streams]
          [compojure])
    (:require [net.cgrand.enlive-html :as en])
    (:import  [java.io InputStream]))


; The server can do so much, for so many leagues.
(defonce leagues (atom {}))

(defn get-league! 
  ([name] (if-let [l (@leagues name)]
            l                 ; We already have a league for this name
            (let [nl (new-league name)] 
              (swap! leagues assoc name nl)
              nl)))
  ([] (get-league! "default")))

(defmacro with-league [name & forms]
  `(binding [*league* (get-league! ~name)]
     ~@forms))


; Raw server code.
(defroutes main-app
  (ANY "/" (html [:h2 "Hello world"]))
  (POST ""))

(defn start-server
  ([port] (run-server {:port port} "/*" (servlet main-app)))
  ([]     (start-server 8080)))