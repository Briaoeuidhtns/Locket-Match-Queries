(ns locket-match-queries.repository
  (:require
   [clojure.edn :as edn]
   [locket-match-queries.api :as api]
   [locket-match-queries.db.system :refer [sql-format query-opts]]
   [locket-match-queries.config :refer [config]]
   [next.jdbc :as jdbc]
   [honeysql.core :as sql]
   [honeysql.helpers :as h]
   [clojure.set :refer [rename-keys]]
   [clojure.set :as set]
   [slingshot.slingshot :refer [throw+]]
   [taoensso.timbre :as log]))

(defn populate-hero-table
  ([db hero-data]
   (jdbc/execute! (db)
                  (-> (h/truncate :hero)
                      sql-format)
                  query-opts)
   (jdbc/execute! (db)
                  (-> (h/insert-into :hero)
                      (h/values (map #(-> %
                                          (rename-keys {:id :hero-id}))
                                  hero-data))
                      sql-format)
                  query-opts)))

(defn populate-item-table
  [db item-data]
  (jdbc/execute! (db)
                 (-> (h/truncate :item)
                     sql-format)
                 query-opts)
  (jdbc/execute! (db)
                 (-> (h/insert-into :item)
                     (h/values (map #(rename-keys % {:id :item-id}) item-data))
                     sql-format)
                 query-opts))

(defn create-pick-ban-entry [])
(defn populate-pick-ban-entries
  [match-data]
  (let [pick-ban-data (get match-data :picks_bans)]
    (doall (for [this-pick-ban (seq pick-ban-data)]
             (create-pick-ban-entry this-pick-ban
                                    (get match-data :match_id))))))

(defn create-additional-unit-entry
  [additional-unit-data match-id account-id]
  ; Currently not grabbing entries correctly
  #_(jdbc/insert! db-spec
                  :additional_unit
                  (-> additional-unit-data
                      (select-keys [:item_0
                                    :item_1
                                    :item_2
                                    :item_3
                                    :item_4
                                    :item_5
                                    :backpack_0
                                    :backpack_1
                                    :backpack_2
                                    :backpack_3
                                    :unitname])
                      (assoc :account_id account-id
                             :match_id match-id))))

(defn populate-additional-unit-table
  [additional-units-data match-id account-id]
  (doall (for [this-data additional-units-data]
           (create-additional-unit-entry (first additional-units-data)
                                         match-id
                                         account-id))))


; Anon radiant account-id 4294967295
; Anon dire account-id 970149193
(def anon-account?
  "Anon account-ids are not unique it turns out"
  #{4294967295 970149193})

(defn create-player-info-entry
  [player-info-data match-id]
  (let [{account-id :account_id} player-info-data]
    (when (not (anon-account? account-id))
      #_(jdbc/insert! db-spec
                      :player_info
                      (-> player-info-data
                          (select-keys [:player_slot
                                        :kills
                                        :deaths
                                        :assists
                                        :leaver_status
                                        :last_hits
                                        :denies
                                        :gold_per_min
                                        :xp_per_min
                                        :hero_id
                                        :item_0
                                        :item_1
                                        :item_2
                                        :item_3
                                        :item_4
                                        :item_5
                                        :backpack_0
                                        :backpack_1
                                        :backpack_2
                                        :backpack_3
                                        :item_neutral])
                          (assoc :account_id account-id
                                 :match_id match-id)))
      (when-let [additional-units (:additional_units player-info-data)]
        (populate-additional-unit-table additional-units
                                        match-id
                                        account-id)))))

(defn populate-player-info-table
  [match-data]
  (let [player-info-data (:players match-data)]
    (doseq [this-player-info player-info-data]
      (create-player-info-entry this-player-info (:match_id match-data)))))

(defn populate-match-tables
  [db matches]
  (jdbc/execute! (db)
                 (-> (h/insert-into :match-table)
                     (h/values (map
                                 #(select-keys %
                                               [:match_id
                                                :radiant_win
                                                :duration
                                                :tower_status_dire
                                                :tower_status_radiant
                                                :barracks_status_dire
                                                :barracks_status_radiant
                                                :first_blood_time
                                                :radiant_score
                                                :dire_score])
                                 matches))
                     sql-format))
  (populate-pick-ban-entries
    db
    (mapcat (fn [{:keys [match_id picks_bans]}]
              (map #(-> %
                        (assoc :match-id match_id)
                        (rename-keys {:team :is-radiant
                                      :order :pick-ban-order}))
                picks_bans))
      matches)))

(defn cache-matches
  [db match-ids]
  (let [needed (set match-ids)
        found (transduce (map :match-table/match-id)
                         conj
                         #{}
                         (jdbc/execute!
                           (db)
                           (-> (h/select :match-table/match-id)
                               (h/from :match-table)
                               (h/where [:in :match-table/match-id match-ids])
                               sql-format)
                           query-opts))
        missing (set/difference needed found)
        data-promises (doall (map (juxt identity api/get-match-data) missing))
        {data false ; TODO set zprint max width for align
         errors true}
        (group-by (comp (partial instance? Throwable) second)
                  (map (fn [[k p]] [k @p]) data-promises))]
    (populate-match-tables data)
    (throw+ "Error fetching some matches"
            (-> (zipmap [:failed :causes] (apply mapv vector errors))
                (assoc :type ::fetch-error)))))
