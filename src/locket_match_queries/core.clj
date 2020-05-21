(ns locket-match-queries.core
  (:require [clojure.edn :as edn]
            [clojure.java.io :as io]
            [clj-http.client :as http]
            [clojure.core.memoize :as memo]
            [locket-match-queries.api :refer :all]
            [locket-match-queries.mock_data :refer :all]
            [clojure.set :as set]
            [locket-match-queries.web :as components]
            [rum.core :as rum]
            [clojure.java.jdbc :as jdbc])
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

(def db-spec
  {:dbtype "mysql"
   :dbname (config :db_name)
   :user (config :db_user)
   :password (config :db_pass)
   :host (config :db_ip)})

(defn create-hero-entry
  [hero-pair]
  (let [id (first hero-pair)
        hero-name (second (re-find #":npc-dota-hero\/(.+)" (str (second hero-pair))))]
    (jdbc/insert! db-spec :hero {:hero_id id :name hero-name})))

(defn populate-hero-table
  ([hero-data]
	;; clear the heros table and then populate, TODO make smarter
   (jdbc/delete! db-spec :hero ["1 = 1"])
   (doall (for [this-data (seq hero-data)] (create-hero-entry this-data)))))

(defn create-item-entry
  [this-item-data]
  (let [itemID (get this-item-data :id)
        name (get this-item-data :name)
        cost (get this-item-data :cost)
        secret_shop (get this-item-data :secret_shop)
        side_shop (get this-item-data :side_shop)
        recipe (get this-item-data :recipe)]

    (jdbc/insert! db-spec :item {:item_id itemID :name name
                                 :cost cost :secret_shop secret_shop
                                 :side_shop side_shop :recipe recipe})))

(defn populate-item-table
  [item-data]
	;; clear the heros table and then populate, TODO make smarter
  (jdbc/delete! db-spec :item ["1 = 1"])
  (doall (for [this-data (seq item-data)] (create-item-entry this-data))))

(defn populate-match-table
  [match-data]
  (jdbc/insert! db-spec :MatchTable {:matchID (get match-data :match_id) :radiantWin (get match-data :radiant_win)
                                     :duration (get match-data :duration) :firstBloodTime (get match-data :first_blood_time)
                                     :towerStatusDire (get match-data :tower_status_dire) :towerStatusRadiant (get match-data :tower_status_radiant)
                                     :barracksStatusDire (get match-data :barracks_status_dire) :barracksStatusRadiant (get match-data :barracks_status_radiant)
                                     :radiantScore (get match-data :radiant_score) :direScore (get match-data :dire_score)
                                     :pickBan 1}))

;(defn populate-match-table
;[match-data]
;(jdbc/insert! db-spec :Match {
;	:matchID (get match-data :match_id)}))

;(defn populate-playerInfo-table
;[data]
;(jdbc/insert! db-spec :Match {:playerSlot {} :isRadiant {} :item0 {} :item1 {} :item2 {}
; :item3 {} :item4 {} :item5 {} :kills {} :deaths {} :assists {} :leaverStatus {}
; :lastHits {} :denies {} :goldPerMinute {} :xpPerMinute {} :playerID {} :matchID {}
; :heroID {}})
;)


(defn -main
  [& args]
  (do
 	(populate-hero-table (get edns :hero-data))
  (populate-item-table (get edns :item-data))))
 	;(populate-match-table (get edns :single-match-data)))

; Search through the list of player ids for the last {} match ids
; Throw them into a set to prevent duplicate pulls
; Iterate through each match id
; 	If it exists in the local database, don't do anything
; 	If not, send out a request and add the result to the local database
; Run stats on the collection of player ids