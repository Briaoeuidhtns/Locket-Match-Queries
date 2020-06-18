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
  ; Currently not grabbing entries correctly
  (jdbc/insert! db-spec :additional_unit {:item_0 (get additional-unit-data :item_0) :item_1 (get additional-unit-data :item_1)
                                          :item_2 (get additional-unit-data :item_2) :item_3 (get additional-unit-data :item_3)
                                          :item_4 (get additional-unit-data :item_4) :item_5 (get additional-unit-data :item_5)
                                          :backpack_0 (get additional-unit-data :backpack_0) :backpack_1 (get additional-unit-data :backpack_1)
                                          :backpack_2 (get additional-unit-data :backpack_2) :backpack_3 (get additional-unit-data :backpack_3)
                                          :unitname (get additional-unit-data :unitname "N/A") :account_id account-id
                                          :match_id match-id})
)

(defn populate-additional-unit-table
  [additional-units-data match-id account-id]
  (doall (for [this-data additional-units-data] (create-additional-unit-entry (first additional-units-data) match-id account-id)))
  )

; Anon account-ids are not unique it turns out

; Anon radiant account-id 4294967295
; Anon dire account-id 970149193
(defn create-player-info-entry
  [player-info-data match-id]
  (let [{account-id :account_id} player-info-data]
    (when (not (#{4294967295 970149193} account-id))
      (jdbc/insert!
        db-spec
        :player_info
        (-> player-info-data
            (select-keys
              [:player_slot :kills :deaths :assists :leaver_status :last_hits
               :denies :gold_per_min :xp_per_min :hero_id :item_0 :item_1
               :item_2 :item_3 :item_4 :item_5 :backpack_0 :backpack_1
               :backpack_2 :backpack_3 :item_neutral])
            (assoc :account_id account-id
                   :match_id match-id)))
      (when-let [additional-units {:additional_units player-info-data}]
        (populate-additional-unit-table additional-units
                                        match-id
                                        account-id)))))

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
