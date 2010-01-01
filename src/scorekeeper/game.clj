(ns scorekeeper.game
    (:use [clojure.set] 
          [util.seq-utils]
          [clojure.contrib.str-utils :only [str-join re-split]])
    (:require [clojure.contrib.str-utils2 :as str])
    (:import [java.util regex.Pattern Date]))

; A league is the core of the abstraction. All these functions expect the bound variable
; *league* to be set to a working league.

(defn new-league 
  ([name] {:tag ::league
           :name name, 
           :players (ref #{})
           :teams   (ref #{})
           :ladder  (ref [])
           :games   (ref '())
           :histories (ref {})})
   ([] (new-league "default")))

(defonce *league* (new-league))
(defonce *ladder-size* 10)


;;; Histories

(defn new-history
  ([wins losses] {:tag ::history :wins wins :losses losses})
  ([] (new-history 0 0)))

(defn inc-history 
  "Produces a new history which is updated by 1 game. Kind should
  be :win/true or :lose/false"
  [history kind] 
  (cond 
   (or (= kind :lose) (not kind)) (new-history 
                                   (:wins history) 
                                   (inc (:losses history)))
   :else (new-history (inc (:wins history)) (:losses history))))

(defn history-for-player [player] 
  (@(:histories *league*) player))

(defn set-history! [player history]
  (dosync
   (alter (:histories *league*) assoc player history)))

;;; Player functions

(defn new-player [name] {:tag ::player :name name})
(defn make-player!
  "Makes a player with a new name and optionally a new history."
  ([name history]
   (let [player (new-player name)]
     (dosync 
      (alter (:players *league*) conj player)
      (alter (:histories *league*) assoc player history))
     player))
  ([name] (make-player! name (new-history))))

(defn- pattern-for-player-search [pattern]
  (cond
   (instance? Pattern pattern)                  pattern
   (= (:tag pattern) ::player)                  (pattern-for-player-search (:name pattern))
   (every? #(Character/isUpperCase %1) pattern) (re-pattern 
                                                 (apply str (interpose "[^A-Z]*" pattern)))
   :else                                        (re-pattern pattern)))

(defn find-player 
  "Finds a player by name. pattern can be a string or a regex.
  If the pattern is all caps, then it is treated as an abbrev."
  [pattern]
  (let [pat (pattern-for-player-search pattern)]
    (first (select #(re-find pat (:name %1)) @(:players *league*)))))

(defn player-nickname [player]
  (apply str (filter #(Character/isUpperCase %1)
                     (:name player))))


;;; Team functions

(defn new-team 
  "A team is simply a set of players."
  [& players]
  (set players))

(defn make-team! [& players]
  (let [new-team (apply new-team players)]
    (dosync
     (alter (:teams *league*)
            conj new-team))
    new-team))

(defn find-teams-by-player [player]
  (let [p (find-player player)]
    (select #(%1 player) @(:teams *league*))))

(defn find-teams-by-members [members-string]
  (let [members-strings (re-split #"/" members-string)
        members         (map find-player members-strings)
        pred            (fn [team] (every? #(team %1) members))]
    (select pred @(:teams *league*))))

;;; Games functions

(defn new-game 
  "Creates a new game. If no date is provided, the current time is used."
  ([winner loser date] {:tag ::game, :winner winner :loser loser :date date})
  ([winner loser]      (new-game winner loser (java.util.Date.))))

(def #^{:arglists '([winner loser] [winner loser date])} 
     make-game! (fn make-game! [& args]
                    (let [game (apply new-game args)]
                      (dosync 
                       (commute (:games *league*) conj game))
                      game)))

;;; Ladder functions

(defn reset-ladder! []
  (dosync (ref-set (:ladder *league*) (vector))))

(defn- new-updated-ladder [lvec winner loser]
  (if (empty? lvec)
    (vector winner loser)
    (let [winnerless-ladder (vec (remove #(= %1 winner) lvec))
          index-of-loser    (index-of-obj winnerless-ladder loser)]
      (vec (take *ladder-size* 
                 (vec-displace winnerless-ladder 
                               index-of-loser 
                               winner))))))

(defn update-ladder! [winner loser]
  (dosync 
   (let [ladder (:ladder *league*)]
     (alter ladder new-updated-ladder winner loser))))

; Composite functions

(defn update-with-game! [game]
  )

;;; Goals:
;;; 1. Add/remove/rename players and track compute their history.
;;; 2. Add/remove teams (which should just be sets of players)
;;; 2a. Find teams with a player(s) in them by name or abbreviation.

