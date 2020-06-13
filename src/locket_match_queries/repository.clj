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
   [slingshot.slingshot :refer [throw+]]))

(defn populate-hero-table
  ([db hero-data]
   (jdbc/execute! (db)
                  (-> (h/truncate :hero)
                      sql-format)
                  query-opts)
   (jdbc/execute! (db)
                  (-> (h/insert-into :hero)
                      (h/values (map #(rename-keys % {:id :hero-id}) hero-data))
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

(defn populate-pick-ban-entries
  [db match-id pick-ban-data]
  (jdbc/execute! (db)
                 (-> (h/insert-into :pick-ban-entry)
                     (h/values (map #(assoc % :match-id match-id)
                                 pick-ban-data))
                     sql-format)
                 query-opts))

(defn populate-match-tables
  [matches]
  (populate-pick-ban-entries
    (map (fn [{:keys [match_id picks_bans]}]
           (map #(assoc % :match-id match_id) picks_bans))
      matches)))
