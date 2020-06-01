(ns locket-match-queries.repository
  (:require [clojure.edn :as edn]
  [locket-match-queries.api :refer :all]
  [locket-match-queries.config :refer :all]
  [clojure.java.jdbc :as jdbc]))


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
  (jdbc/insert! db-spec :match_table {:match_id (get match-data :match_id) :radiant_win (get match-data :radiant_win)
                                     :duration (get match-data :duration) :first_blood_time (get match-data :first_blood_time)
                                     :tower_status_dire (get match-data :tower_status_dire) :tower_status_radiant (get match-data :tower_status_radiant)
                                     :barracks_status_dire (get match-data :barracks_status_dire) :barracks_status_radiant (get match-data :barracks_status_radiant)
                                     :radiant_score (get match-data :radiant_score) :dire_score (get match-data :dire_score)}))

(defn create-pick-ban-entry
[pick-ban-data match_id]
(jdbc/insert! db-spec :pick_ban_entry {:match_id match_id :hero_id (get pick-ban-data :hero_id)
																																							:is_pick (get pick-ban-data :is_pick) :is_radiant (get pick-ban-data :team)
																																							:pick_ban_order (get pick-ban-data :order)})
)

(defn populate-pick-ban-entries
[match-data]
(let [pick-ban-data (get match-data :picks_bans )]
	(doall (for [this-pick-ban (seq pick-ban-data)] (create-pick-ban-entry this-pick-ban (get  match-data :match_id))))
	 ))


	(defn populate-match-tables
		([match-data]
		))