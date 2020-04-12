(ns locket-match-queries.core
  (:require [clojure.edn :as edn]
            [clojure.java.io :as io]
            [clj-http.client :as http]
            [clojure.core.memoize :as memo]
            [locket-match-queries.api :refer :all]
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

(def dummy-match-data (-> "matchData.edn" slurp edn/read-string))

(def db-spec
  {:dbtype "mysql"
   :dbname (config :db_name)
   :user (config :db_user)
   :password (config :db_pass)})

;<<<<<<< HEAD
(defn extract-heroes
[result]
(map :hero_id result))

(defn create-hero-entry
[hero-pair & left]
	(let [id (first hero-pair)
							hero-name (second (re-find #":npc-dota-hero\/(.+)" (str (second hero-pair))))]
					(jdbc/insert! db-spec :Heros {:heroID id :name hero-name})
					(if left (apply create-hero-entry left)
	)
)
	)

(defn populate-heros-table
[hero-data]
	;; clear the heros table and then populate TODO make smarter
		(jdbc/delete! db-spec :Heros ["1 = 1"])   
		(apply create-hero-entry (seq hero-data))
)


(defn -main
  [& args]
  (let 		[match-data  (-> "matchData.edn" slurp edn/read-string)
          player-data  (-> "playerData.edn" slurp edn/read-string)
          hero-data  (-> "heroData.edn" slurp edn/read-string)]
 	(populate-heros-table hero-data)
  )
  )

;=======
;(defn hero-stats
;  {:author "Brian"}
;  [player-stats]
;  (sort-by (comp - second)
;           (set/rename-keys (frequencies (map :hero_id player-stats))
;                            (heroes))))

;(defn -main
;  {:author "Brian"}
;  [& player-ids]
;  (spit "stats.html"
;        (rum/render-static-markup
;         (components/hero-stat-list (hero-stats (players dummy-match-data))))))
;>>>>>>> 132bb02fc8be956515d2e0026a99bbac3adf767e