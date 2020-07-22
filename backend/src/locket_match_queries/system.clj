(ns locket-match-queries.system
  (:refer-clojure :exclude [new key])
  (:require
   [locket-match-queries.db.system :as db]
   [locket-match-queries.server :as server]
   [locket-match-queries.schema :as schema]))

(defn new [{:keys [db key]}] (merge (db/new-comp db) (schema/new) (server/new)))
