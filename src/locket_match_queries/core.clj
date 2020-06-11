(ns locket-match-queries.core
  (:require
   [clojure.edn :as edn]
   [clojure.java.io :as io]
   [clj-http.client :as http]
   [clojure.core.memoize :as memo]
   [locket-match-queries.api :refer :all]
   [locket-match-queries.mock_data :refer :all]
   [locket-match-queries.repository :refer :all]
   [clojure.set :as set]
   [locket-match-queries.web :as components]
   [rum.core :as rum])
  (:import
   (java.time Duration))
  (:gen-class))

(defn extract-match-ids {:author "Matthew"} [result] (map :match_id result))

(defn team-recent-matches
  {:author "Brian"}
  [team-members]
  (into #{}
        (flatten (map (comp extract-match-ids recent-matches) team-members))))

(defn players {:author "Matthew"} [result] (mapcat :players result))

;(defn populate-match-table
;[match-data]
;(jdbc/insert! db-spec :Match {
;       :matchID (get match-data :match_id)}))

;(defn populate-playerInfo-table
;[data]
;(jdbc/insert! db-spec :Match {:playerSlot {} :isRadiant {} :item0 {} :item1 {}
;:item2 {}
; :item3 {} :item4 {} :item5 {} :kills {} :deaths {} :assists {} :leaverStatus
; {}
; :lastHits {} :denies {} :goldPerMinute {} :xpPerMinute {} :playerID {}
; :matchID {}
; :heroID {}})
;)


(defn -main
  [& args]
  (do (populate-hero-table (get edns :hero-data))
      (populate-item-table (get edns :item-data))
      (populate-match-table (get edns :single-match-data))
      (populate-pick-ban-entries (get edns :single-match-data))))

; Search through the list of player ids for the last {} match ids
; Throw them into a set to prevent duplicate pulls
; Iterate through each match id
;       If it exists in the local database, don't do anything
;       If not, send out a request and add the result to the local database
; Run stats on the collection of player ids