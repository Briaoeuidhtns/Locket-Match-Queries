(ns locket-match-queries.db.system
  (:require
   [honeysql.core :as sql]
   [next.jdbc.connection :as connection]
   [taoensso.timbre :as log])
  (:import
   (com.zaxxer.hikari HikariDataSource)))

(def ^:private default-config {:dbtype "mysql"})

(defn new-comp
  ([] (new-comp nil))
  ([config?]
   {:db (connection/component HikariDataSource
                              (merge default-config config?))}))

(defn sql-format
  [sql-map]
  (let [q (sql/format sql-map :namespace-as-table? true :quoting :mysql)]
    (log/debug q)
    q))
