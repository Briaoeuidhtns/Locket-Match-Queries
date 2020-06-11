(ns locket-match-queries.repository
  (:require
   [clojure.edn :as edn]
   [locket-match-queries.api :refer :all]
   [locket-match-queries.config :refer :all]
   [clojure.java.jdbc :as jdbc]
   [clojure.set :refer [rename-keys]]))


(def db-spec
  {:dbtype "mysql"
   :dbname (config :db_name)
   :user (config :db_user)
   :password (config :db_pass)
   :host (config :db_ip)})

(defn create-hero-entry
  [[id hero-name]]
  (jdbc/insert! db-spec :hero {:hero_id id :name (name hero-name)}))

(defn populate-hero-table
  ([hero-data]
   ;; clear the heros table and then populate, TODO make smarter
   (jdbc/delete! db-spec :hero ["1 = 1"])
   (doseq [this-data hero-data] (create-hero-entry this-data))))

(defn create-item-entry
  [this-item-data]
  (jdbc/insert! db-spec
                :item
                (-> this-item-data
                    (select-keys
                      [:item_id :name :cost :secret_shop :side_shop :recipe])
                    (rename-keys {:id :item_id}))))

(defn populate-item-table
  [item-data]
  ;; clear the heros table and then populate, TODO make smarter
  (jdbc/delete! db-spec :item ["1 = 1"])
  (doseq [this-data item-data] (create-item-entry this-data)))

(defn populate-match-table
  [match-data]
  (jdbc/insert! db-spec
                :match_table
                (select-keys match-data
                             [:match_id
                              :radiant_win
                              :duration
                              :first_blood_time
                              :tower_status_dire
                              :tower_status_radiant
                              :barracks_status_dire
                              :barracks_status_radiant
                              :radiant_score
                              :dire_score])))

(defn create-pick-ban-entry
  [pick-ban-data match-id]
  (jdbc/insert! db-spec
                :pick_ban_entry
                (-> pick-ban-data
                    (select-keys [:hero_id :is_pick :team :order])
                    (rename-keys {:team :is_radiant})
                    (assoc :match_id match-id))))

(defn populate-pick-ban-entries
  [match-data]
  (let [pick-ban-data (get match-data :picks_bans)]
    (doseq [this-pick-ban pick-ban-data]
      (create-pick-ban-entry this-pick-ban (:match_id match-data)))))


(defn populate-match-tables ([match-data]))
