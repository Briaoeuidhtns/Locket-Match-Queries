(ns locket-match-queries.system
  (:refer-clojure :exclude [new])
  (:require
   [locket-match-queries.db.system :as db]))

(defn new [{:keys [db]}] (merge (db/new-comp db)))
