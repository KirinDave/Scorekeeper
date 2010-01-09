(ns scorekeeper.web.helpers
    (:use [scorekeeper [game :only (*league* new-league with-league)]])
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

(defmacro in-league! [league-name & body]
  `(with-league (get-league! ~league-name)
                ~@body))


(defmacro RESOURCE 
  "For use in routes* macros, the extension is placed into regexp which is used
  to grab files that end that way and to map into ./site/_mapping_."
  [extension mapping]
  (let [pattern (re-pattern (str "/(.+\\." extension ")$"))
        loc     (str "./site/" mapping)]
    `(compojure/GET ~pattern (or (java.io.File. ~loc ((:route-params ~'request) 0))
                       (compojure/page-not-found))))) 

(defn with-leagues 
  "Works under a session handler, making leagues from session easy to set."
  [handler]
  (fn [request]
      (let [session     (:session request)
            league-name (or (:league-name session) "default")
            request     (-> request
                            (assoc :league-name league-name))
            response    (in-league! league-name 
                                    (handler request))]
        (println "Response is" response "\nleague-name is " league-name)
        (if-let [new-league-name (:league-name response)]
            (assoc response :session 
                   (assoc (or (:session response) session {}) 
                          :league-name new-league-name))
            response))))
