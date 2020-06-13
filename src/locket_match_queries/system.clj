(ns locket-match-queries.system
  (:require
   [com.stuartsierra.component :as component]
   [locket-match-queries.db.system :as db]))

(defn new [{:keys [db]}] (merge (db/new-comp db)))
