(ns scorekeeper.web 
    (:use [scorekeeper.game]
          [scorekeeper.web helpers]
          [clojure.contrib str-utils seq-utils duck-streams]
          [compojure])
    (:require [net.cgrand.enlive-html :as en]
              [scorekeeper.web.templates :as t])
    (:import  [java.io InputStream File]))


;; (defn with-league-from-session [fun]
;;   (fn [request] 
;;       (in-league! (or (:league (:session request)) "default")
;;                   (fun request))))



(defn dump-sesh [request]
  (let [session (request :session)
        league-name (request :league-name)]
    {:body (str "<html><head><title>ok</title></head></body>Seshtown: " session
                "<hr>League-name: " league-name)
    :session (assoc session :x (inc (or (:x session) 0)))
    :league-name "cowabunga" }))

(defn stateful [request-fn]
  (-> request-fn with-leagues with-session))

;;; Actions

(defn homepage [request] 
  (t/league-home request))

(defn test-form [request]
  {:body (str (:params request))})

(defn add-player-action [request]
  (when-let [pname (-> request :params :pname)]
      (get-player! pname))
  (redirect-to "/"))

(defn add-team-action [request]
  (when-let [tname (-> request :params :tname)]
      (get-team! tname))
  (redirect-to "/"))


(defn req-set-league [request]
  {:status 302 
   :league-name (or (-> request :params :lname) "default") 
   :headers { "Refresh" "0; /" }})

(defroutes main-app
  (RESOURCE "css" "css")
  (RESOURCE "js" "javascript")
  (RESOURCE "(png|jpg|gif)" "images")
  (POST "/add_player" (stateful add-player-action))
  (POST "/add_team"   (stateful add-team-action))
  (GET "/set-league/:lname"  (stateful req-set-league))  
  (ANY "/"  (stateful homepage))
  (GET "*" "Fell Through!"))


(defn start-server
  ([port] (run-server {:port port} "/*" (servlet main-app)))
  ([]     (start-server 8080)))
