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