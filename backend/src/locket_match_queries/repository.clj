(ns locket-match-queries.repository
  (:require
   [clojure.set :as set :refer [rename-keys]]
   [clojure.spec.alpha :as s]
   [honeysql.helpers :as h]
   [locket-match-queries.api :as api]
   [locket-match-queries.api.spec.hero :as hero]
   [locket-match-queries.api.spec.item :as item]
   [locket-match-queries.api.spec.match :as match]
   [locket-match-queries.api.spec.player :as player]
   [locket-match-queries.db.system :refer [sql-format]]
   [next.jdbc :as jdbc]
   [slingshot.slingshot :refer [throw+]]
   [spec-tools.core :refer [select-spec]]
   [taoensso.timbre :as log]))

(defn populate-hero-table
  ([db hero-data]
   (jdbc/execute! (db)
                  (-> (h/truncate :hero)
                      sql-format)
                  jdbc/snake-kebab-opts)
   (jdbc/execute! (db)
                  (-> (h/insert-into :hero)
                      (h/values (map #(-> %
                                          (rename-keys {:id :hero-id})
                                          (update :name name))
                                  hero-data))
                      sql-format)
                  jdbc/snake-kebab-opts)))
(s/fdef populate-hero-table
  :args (s/cat :db :next.jdbc.specs/connectable
               :hero-data (s/coll-of ::hero/hero)))

(defn populate-item-table
  [db item-data]
  (jdbc/execute! (db)
                 (-> (h/truncate :item)
                     sql-format)
                 jdbc/snake-kebab-opts)
  (jdbc/execute! (db)
                 (-> (h/insert-into :item)
                     (h/values (map #(rename-keys % {:id :item-id}) item-data))
                     sql-format)
                 jdbc/snake-kebab-opts))
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
    (jdbc/execute! (db)
                   (-> (h/insert-into :pick-ban-entry)
                       (h/values entries)
                       sql-format)
                   jdbc/snake-kebab-opts)))
(s/fdef populate-pick-ban-entries
  :args (s/cat :db :next.jdbc.specs/connectable
               :matches (s/coll-of ::match/match)))

(defn populate-additional-unit-table
  [db additional-units]
  (jdbc/execute! (db)
                 (-> (h/insert-into :additional-unit)
                     (h/values additional-units)
                     sql-format)
                 jdbc/snake-kebab-opts))
(s/fdef populate-additional-unit-table
  :args (s/cat :db :next.jdbc.specs/connectable
               :additional-units
                 (s/coll-of (s/merge ::player/additional-units
                                       (s/keys :req-un [::player/account_id
                                                        ::match/match_id])))))

; Anon radiant account-id 4294967295
; Anon dire account-id 970149193
(def anon-account?
  "Anon account-ids are not unique it turns out"
  #{4294967295 970149193})

(s/def ::unique-account-id (s/and pos-int? (complement anon-account?)))

(defn populate-player-info-table
  [db matches]
  (let [players-data (filter (comp not anon-account? :account_id)
                       (mapcat (fn [{:keys [match_id players]}]
                                 (map #(assoc % :match_id match_id)
                                   (select-spec ::player/players players)))
                         matches))
        enterable-players (map #(dissoc % :additional_units) players-data)
        additional-units
        (filter identity
          (mapcat (fn [{:keys [additional_units match_id account_id]}]
                    (map #(as-> % $
                            (select-spec ::player/additional-units $)
                            (assoc $
                              :match_id match_id
                              :account_id account_id))
                      additional_units))
            players-data))]
    (jdbc/execute! (db)
                   (-> (h/insert-into :player-info)
                       (h/values enterable-players)
                       sql-format)
                   jdbc/snake-kebab-opts)
    (when (seq additional-units)
      (populate-additional-unit-table db additional-units))))
(s/fdef populate-player-info-table
  :args (s/cat :db :next.jdbc.specs/connectable
               :matches (s/coll-of ::match/match)))

(defn populate-match-tables
  [db matches]
  (jdbc/execute!
    (db)
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
                           jdbc/snake-kebab-opts))
        missing (set/difference needed found)
        data-promises (doall (map (juxt identity api/get-match-data) missing))
        {data false errors true} (group-by
                                   (comp (partial instance? Throwable) second)
                                   (map (fn [[k p]] [k @p]) data-promises))]
    (populate-match-tables db data)
    (throw+ "Error fetching some matches"
            (-> (zipmap [:failed :causes] (apply mapv vector errors))
                (assoc :type ::fetch-error)))))
