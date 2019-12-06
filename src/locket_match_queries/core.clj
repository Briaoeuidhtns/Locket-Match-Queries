(ns locket-match-queries.core
  (:require [clojure.edn :as edn]
            [clojure.java.io :as io]
            [clj-http.client :as http]
            [clojure.core.memoize :as memo]
            [locket-match-queries.api :refer :all]
            [clojure.set :as set]
            [locket-match-queries.web :as components]
            [rum.core :as rum])
  (:import (java.time Duration))
  (:gen-class))

;; Matthew
(defn extract-match-ids
  [result]
  (map :match_id result))

;; Brian
(defn team-recent-matches
  [team-members]
  (into #{} (flatten (map (comp extract-match-ids recent-matches) team-members))))

;; Matthew
(defn get-matches-data
  [match_ids]
  (map get-match-data match_ids))

;; Matthew
(defn players
  [result]
  (mapcat :players result))

(def dummy-match-data (-> "matchData.txt" slurp edn/read-string))

;; Brian
(defn hero-stats
  [player-stats]
  (sort-by (comp - second)
           (set/rename-keys (frequencies (map :hero_id player-stats))
                            (heroes))))

(defn -main
  [& player-ids]
  (spit "stats.html"
        (rum/render-static-markup
         (components/hero-stat-list (hero-stats (players dummy-match-data))))))
