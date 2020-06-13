(ns locket-match-queries.db.system
  (:require
   [com.stuartsierra.component :as component]
   [honeysql.core :as sql]
   [next.jdbc :as jdbc]
   [next.jdbc.connection :as connection]
   [next.jdbc.result-set :as result-set]
   [taoensso.timbre :as log]
   [clojure.string :as str])
  (:import
   (com.zaxxer.hikari HikariDataSource)))

(def ^:private default-config {:dbtype "mysql"})

(defn as-kebab-maps
  [rs opts]
  (let [kebab #(str/replace % #"_" "-")]
    (result-set/as-modified-maps rs
                                 (assoc opts
                                   :qualifier-fn kebab
                                   :label-fn kebab))))

(def query-opts {:builder-fn as-kebab-maps})

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
