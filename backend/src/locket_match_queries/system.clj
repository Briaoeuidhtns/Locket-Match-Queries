(ns locket-match-queries.system
  (:refer-clojure :exclude [new key])
  (:require
   [locket-match-queries.db.system :as db]
   [locket-match-queries.server :as server]
   [locket-match-queries.schema :as schema]))

(defn new
  [{db-config :db api-key :key}]
  (merge (db/new-comp db-config) (server/new api-key)))
