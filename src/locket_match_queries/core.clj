(ns locket-match-queries.core
  (:require [clojure.edn :as edn]
            [clojure.java.io :as io]
            [clj-http.client :as http]
            [clojure.core.memoize :as memo]
            [locket-match-queries.api :refer :all]
            [locket-match-queries.mock_data :refer :all]
            [locket-match-queries.repository :refer :all]
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

(defn players
  {:author "Matthew"}
  [result]
  (mapcat :players result))

(defn -main
  [& args]
  ;(let [match-data (get edns :single-match-data)]
  (let [match-data (get edns :other-match-data)]
    (do
      (populate-hero-table (get edns :hero-data))
      (populate-item-table (get edns :item-data))
      (populate-match-table match-data)
      (populate-pick-ban-entries match-data)
      (populate-player-info-table match-data))))

; Search through the list of player ids for the last {} match ids
; Throw them into a set to prevent duplicate pulls
; Iterate through each match id
; 	If it exists in the local database, don't do anything
; 	If not, send out a request and add the result to the local database
; Run stats on the collection of player ids