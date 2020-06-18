(ns locket-match-queries.repository
  (:require [clojure.edn :as edn]
            [locket-match-queries.api :refer :all]
            [locket-match-queries.config :refer :all]
            [clojure.java.jdbc :as jdbc]
            [clojure.tools.trace :as trace]))

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
  (let [item_id (get this-item-data :id)
        name (get this-item-data :name)
        cost (get this-item-data :cost)
        secret_shop (get this-item-data :secret_shop)
        side_shop (get this-item-data :side_shop)
        recipe (get this-item-data :recipe)]

    (jdbc/insert! db-spec :item {:item_id item_id :name name
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
  [pick-ban-data match-id]
  (jdbc/insert! db-spec :pick_ban_entry {:match_id match-id :hero_id (get pick-ban-data :hero_id)
                                         :is_pick (get pick-ban-data :is_pick) :is_radiant (get pick-ban-data :team)
                                         :pick_ban_order (get pick-ban-data :order)}))

(defn populate-pick-ban-entries
  [match-data]
  (let [pick-ban-data (get match-data :picks_bans)]
    (doall (for [this-pick-ban (seq pick-ban-data)] (create-pick-ban-entry this-pick-ban (get  match-data :match_id))))))

(defn create-additional-unit-entry
  [additional-unit-data match-id account-id]
  (print "AAAA\n")
  ;(jdbc/insert! db-spec :additional_unit {:item_0 (get additional-unit-data :item_0) :item_1 (get additional-unit-data :item_1)
  ;                                        :item_2 (get additional-unit-data :item_2) :item_3 (get additional-unit-data :item_3)
  ;                                        :item_4 (get additional-unit-data :item_4) :item_5 (get additional-unit-data :item_5)
  ;                                        :backpack_0 (get additional-unit-data :backpack_0) :backpack_1 (get additional-unit-data :backpack_1)
  ;                                        :backpack_2 (get additional-unit-data :backpack_2) :backpack_3 (get additional-unit-data :backpack_3)
  ;                                        :unitname (get additional-unit-data :unitname "AAAAAAAAAAAA") :account_id account-id
  ;                                        :match_id match-id})
)

(defn populate-additional-unit-table
  [additional-units-data match-id account-id]
  (let [mod-list (list additional-units-data)]
  (doall (for [this-data mod-list] (print "CCCC\n")))
  )
  ;(print (get (first additional-units-data) :unitname)) 
  ;(doall (for [this-data (seq additional-units-data)] 
  ;(create-additional-unit-entry (first additional-units-data) match-id account-id)
  ;)) 
  
  )

; Anon account-ids are not unique it turns out 

; Anon radiant account-id 4294967295
; Anon dire account-id 970149193
(defn create-player-info-entry
  [player-info-data match-id]
  (let [account-id (get player-info-data :account_id)]
    (if (and (not= account-id 4294967295) (not= account-id 970149193))
      (doall
       (jdbc/insert! db-spec :player_info {:match_id match-id
                                           :player_slot (get player-info-data :player_slot)
                                           :kills (get player-info-data :kills)
                                           :deaths (get player-info-data :deaths)
                                           :assists (get player-info-data :assists)
                                           :leaver_status (get player-info-data :leaver_status)
                                           :last_hits (get player-info-data :last_hits)
                                           :denies (get player-info-data :denies)
                                           :gold_per_min (get player-info-data :gold_per_min) :xp_per_min (get player-info-data :xp_per_min)
                                           :account_id account-id :hero_id (get player-info-data :hero_id)
                                           :item_0 (get player-info-data :item_0) :item_1 (get player-info-data :item_1)
                                           :item_2 (get player-info-data :item_2) :item_3 (get player-info-data :item_3)
                                           :item_4 (get player-info-data :item_4) :item_5 (get player-info-data :item_5)
                                           :backpack_0 (get player-info-data :backpack_0) :backpack_1 (get player-info-data :backpack_1)
                                           :backpack_2 (get player-info-data :backpack_2) :backpack_3 (get player-info-data :backpack_3)
                                           :item_neutral (get player-info-data :item_neutral) })

       (if (get player-info-data :additional_units) (populate-additional-unit-table (get player-info-data :additional_units) match-id account-id))
       ))))

(defn populate-player-info-table
  [match-data]
  (let [player-info-data (get match-data :players)]
 ;(trace/dotrace
    (doall
     (for [this-player-info (seq player-info-data)]
       (create-player-info-entry this-player-info (get match-data :match_id))))
	;)
    ))

(defn populate-match-tables
  ([match-data]))
