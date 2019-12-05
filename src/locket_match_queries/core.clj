(ns locket-match-queries.core
  (:require [clojure.edn :as edn]
            [clojure.java.io :as io]
            [clj-http.client :as http]
            [clojure.core.memoize :as memo]
            [locket-match-queries.api :refer :all]
            )
  (:import (java.time Duration))
  (:gen-class))

(defn extract-match-ids
  [result]
  (map :match_id result))

;; Brian
(defn team-recent-matches
  [team-members]
  (into #{} (flatten (map (comp extract-match-ids recent-matches) team-members))))

(defn get-matches-data
  [match_ids]
  (map get-match-data match_ids))

(defn extract-players
  [result]
  (map :players result))


;; (defn -main
;;   [& args]
;;   (let {key :key player_ids :player_ids} config
;;           match-data  (-> "matchData.txt" slurp edn/read-string)))
