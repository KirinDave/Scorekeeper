(ns scorekeeper.web.templates
    (:use [scorekeeper game]
          [scorekeeper.web helpers]
          [clojure.contrib str-utils seq-utils duck-streams]
          [compojure])
    (:require [net.cgrand.enlive-html :as en])
    (:import  [java.io File]))


(en/deftemplate league-home (File. "./site" "index.html") [request]
   [:#roster :ul :li] (en/clone-for [item @(:players *league*)] 
                                    (en/content (:name item)))
   [:#rankings :ol :li] (en/clone-for [team @(:ladder *league*)]
                                      (en/content (team-nickname team)))
   [:#leagues :ul :li]  (en/clone-for [lname (sort (keys @leagues))]
                                      (en/html-content (link-league lname)))
   [:#league-name] (en/content (:league-name request))
)