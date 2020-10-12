(ns locket-match-queries.repository
  (:require
   [clojure.core.async :as a :refer [go <!]]
   [clojure.set :as set :refer [rename-keys]]
   [clojure.spec.alpha :as s]
   [honeysql.helpers :as h]
   [locket-match-queries.db.queries :as query]
   [locket-match-queries.api :as api]
   [locket-match-queries.api.spec.hero :as hero]
   [locket-match-queries.api.spec.item :as item]
   [locket-match-queries.api.spec.match :as match]
   [locket-match-queries.api.spec.player :as player]
   [locket-match-queries.db.system :refer [sql-format]]
   [next.jdbc :as jdbc]
   [slingshot.slingshot :refer [throw+]]
   [spec-tools.core :refer [select-spec]]
   [honeysql.format :as fmt]
   [honeysql.core :as sql]
   [taoensso.timbre :as log]))

(defn populate-hero-table
  ([db hero-data]
   (jdbc/execute! db
                  (-> (h/insert-into :hero)
                      (h/values (map #(-> %
                                          (rename-keys {:id :hero-id})
                                          (update :name name))
                                  hero-data))
                      sql-format))))
(s/fdef populate-hero-table
  :args (s/cat :db :next.jdbc.specs/connectable
               :hero-data (s/coll-of ::hero/hero)))

(defn populate-item-table
  [db item-data]
  (jdbc/execute! db
                 (-> (h/insert-into :item)
                     (h/values (map #(rename-keys % {:id :item-id}) item-data))
                     sql-format)))
(s/fdef populate-item-table
  :args (s/cat :db :next.jdbc.specs/connectable
               :item-data (s/coll-of ::item/item)))

(defn populate-pick-ban-entries
  [db matches]
  (let [entries (mapcat (fn [{:keys [match_id picks_bans]}]
                          (map #(-> %
                                    (assoc :match-id match_id)
                                    (rename-keys {:team :is-radiant
                                                  :order :pick-ban-order}))
                            picks_bans))
                  matches)]
    (jdbc/execute! db
                   (-> (h/insert-into :pick-ban-entry)
                       (h/values entries)
                       sql-format))))
(s/fdef populate-pick-ban-entries
  :args (s/cat :db :next.jdbc.specs/connectable
               :matches (s/coll-of ::match/match)))

(defn populate-additional-unit-table
  [db additional-units]
  (jdbc/execute! db
                 (-> (h/insert-into :additional-unit)
                     (h/values additional-units)
                     sql-format)))
(s/fdef populate-additional-unit-table
  :args (s/cat :db :next.jdbc.specs/connectable
               :additional-units
                 (s/coll-of (s/merge ::player/additional-units
                                       (s/keys :req-un [::player/player_slot
                                                        ::match/match_id])))))

; Anon radiant account-id 4294967295
; Anon dire account-id 970149193
(def anon-account?
  "Anon account-ids are not unique it turns out"
  #{4294967295 970149193})

(s/def ::unique-account-id (s/and pos-int? (complement anon-account?)))

(defn populate-player-info-table
  [db matches]
  ;; REVIEW should we really remove anon data?
  ;; The db now uses slot+match to identify players
  (let [players-data (filter (comp not anon-account? :account_id)
                       (mapcat (fn [{:keys [match_id players]}]
                                 (map #(assoc % :match_id match_id)
                                   (select-spec ::player/players players)))
                         matches))
        enterable-players (map #(dissoc % :additional_units) players-data)
        additional-units
        (filter identity
          (mapcat (fn [{:keys [additional_units player_slot match_id]}]
                    (map #(as-> % $
                            (select-spec ::player/additional-units $)
                            (assoc $
                              :match_id match_id
                              :player_slot player_slot))
                      additional_units))
            players-data))]
    ;; TODO add indicate whether this match was fetched for this user
    ;; so we don't miss matches when getting recent >match id and they were in a
    ;; match already fetched
    (jdbc/execute! db
                   (-> (h/insert-into :player-info)
                       (h/values enterable-players)
                       sql-format))
    (when (seq additional-units)
      (populate-additional-unit-table db additional-units))))
(s/fdef populate-player-info-table
  :args (s/cat :db :next.jdbc.specs/connectable
               :matches (s/coll-of ::match/match)))

(defn populate-match-tables
  [db matches]
  (jdbc/execute!
    db
    (-> (h/insert-into :match-table)
        (h/values
          (map #(dissoc (select-spec ::match/match %) :players :picks_bans)
            matches))
        sql-format))
  (populate-pick-ban-entries db matches)
  (populate-player-info-table db matches))
(s/fdef populate-match-tables
  :args (s/cat :db :next.jdbc.specs/connectable
               :matches (s/coll-of ::match/match)))

;; copypasted from honeysql's :insert-into
(defmethod fmt/format-clause :replace-into
  [[_ table] _]
  (if (and (sequential? table) (sequential? (first table)))
    (str "REPLACE INTO "
         (fmt/to-sql (ffirst table))
         (binding [fmt/*namespace-as-table?* false]
           (str " ("
                (fmt/comma-join (map fmt/to-sql (second (first table))))
                ") "))
         (binding [fmt/*subquery?* false] (fmt/to-sql (second table))))
    (str "REPLACE INTO " (fmt/to-sql table))))
(fmt/register-clause! :replace-into 60)

(defn ensure-cached!
  [db api-key user-ids]
  (a/go
    (let [missing (->> user-ids
                       (map (fn [id]
                              (api/matches-for-since
                                api-key
                                id
                                (query/most-recent-pulled-for db id))))
                       a/merge
                       (a/into #{})
                       <!)
          res (->> missing
                   (map (partial api/get-match-data api-key :match_id))
                   a/merge
                   (a/into [])
                   <!)
          newest (reduce max missing)
          {data false errors true} (group-by (partial instance? Throwable) res)]
      (populate-match-tables db data)
      (jdbc/execute-one! db
                         (sql-format
                           {:replace-into :user-meta
                            :columns [:account-id :most-recent-pulled]
                            :values (map vector user-ids (repeat newest))}))
      (when errors
        (throw+ "Error fetching some matches"
                {:type ::fetch-error :errors errors})))))
