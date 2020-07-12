(ns locket-match-queries.db.queries
  (:require
   [next.jdbc :as jdbc]
   [clojure.spec.alpha :as s]
   [locket-match-queries.api.spec.hero :as hero]
   [locket-match-queries.db.system :refer [sql-format]]
   [honeysql.helpers :as h]))

(defn get-heroes
  [db]
  (jdbc/execute! (db)
                 (-> (h/select [:hero-id :id] :name)
                     (h/from :hero)
                     sql-format)
                 jdbc/unqualified-snake-kebab-opts))
(s/fdef get-heroes
  :args (s/cat :db :next.jdbc.specs/connectable)
  :ret (s/coll-of ::hero/hero))
