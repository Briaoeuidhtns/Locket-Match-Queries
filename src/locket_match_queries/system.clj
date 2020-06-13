(ns locket-match-queries.system
  (:require
   [com.stuartsierra.component :as component]
   [locket-match-queries.db.system :as db]))

(defn new [] (merge (db/new-comp)))
