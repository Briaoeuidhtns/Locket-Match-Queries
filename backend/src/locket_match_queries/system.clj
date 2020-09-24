(ns locket-match-queries.system
  (:refer-clojure :exclude [new key])
  (:require
   [locket-match-queries.db.system :as db]
   [locket-match-queries.server :as server]
   [locket-match-queries.api :as api]))

(defn new
  [{db-config :db api-key :key}]
  (merge (db/new-comp db-config)
         (server/new)
         (api/new-default-limit-gen api-key)))
