(ns scorekeeper.web 
    (:use [scorekeeper.game]
          [scorekeeper.web helpers templates]
          [clojure.contrib str-utils seq-utils duck-streams]
          [compojure])
    (:require [net.cgrand.enlive-html :as en])
    (:import  [java.io InputStream File]))


(defroutes main-app
  (RESOURCE "css" "css")
  (RESOURCE "js" "javascript")
  (RESOURCE "(png|jpg|gif)" "images")
  (ANY "/" (with-league (get-league! "default") 
                        (league-home)))
  (ANY "*" "Fell Through!"))


(defn start-server
  ([port] (run-server {:port port} "/*" (servlet main-app)))
  ([]     (start-server 8080)))