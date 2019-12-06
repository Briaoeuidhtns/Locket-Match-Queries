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

(defn extract-match-ids
  {:author "Matthew"}
  [result]
  (map :match_id result))

(defn team-recent-matches
  {:author "Brian"}
  [team-members]
  (into #{} (flatten (map (comp extract-match-ids recent-matches) team-members))))

(defn get-matches-data
  [match_ids]
  (map get-match-data match_ids))

(defn players
  {:author "Matthew"}
  [result]
  (mapcat :players result))

(def dummy-match-data (-> "matchData.txt" slurp edn/read-string))

(defn hero-stats
  {:author "Brian"}
  [player-stats]
  (sort-by (comp - second)
           (set/rename-keys (frequencies (map :hero_id player-stats))
                            (heroes))))

(defn -main
  {:author "Brian"}
  [& player-ids]
  (spit "stats.html"
        (rum/render-static-markup
         (components/hero-stat-list (hero-stats (players dummy-match-data))))))
