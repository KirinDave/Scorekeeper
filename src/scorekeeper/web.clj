(ns scorekeeper.web 
    (:use [scorekeeper.game]
          [scorekeeper.web helpers templates]
          [clojure.contrib str-utils seq-utils duck-streams]
          [compojure])
    (:require [net.cgrand.enlive-html :as en])
    (:import  [java.io InputStream File]))


;; (defn with-league-from-session [fun]
;;   (fn [request] 
;;       (in-league! (or (:league (:session request)) "default")
;;                   (fun request))))



(defn dump-sesh [request]
  (let [session (request :session)
        league-name (request :league-name)]
    (println "Test")
    (println (redirect-to "/bunk"))
    {:body (str "<html><head><title>ok</title></head></body>Seshtown: " session
                "<hr>League-name: " league-name)
    :session (assoc session :x (inc (or (:x session) 0)))
    :league-name "cowabunga" }))

(defn req-set-league [request]
  [302 ])

(defn stateful [request-fn]
  (-> request-fn with-leagues with-session))

(defroutes main-app
  (RESOURCE "css" "css")
  (RESOURCE "js" "javascript")
  (RESOURCE "(png|jpg|gif)" "images")
  (GET "/t1"  (stateful dump-sesh))
  (ANY "/" (with-league (get-league! "default") 
                        (league-home)))
  (ANY "*" "Fell Through!"))


(defn start-server
  ([port] (run-server {:port port} "/*" (servlet main-app)))
  ([]     (start-server 8080)))
