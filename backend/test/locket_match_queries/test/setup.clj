(ns locket-match-queries.test.setup
  (:require
   [locket-match-queries.api :as api]
   [locket-match-queries.db.system :as db.system]
   [locket-match-queries.test.dbowner :refer [container]]
   [next.jdbc :as jdbc]
   [com.stuartsierra.component :as component]))

(def ^:dynamic *system* {})

(defn db
  [f]
  (binding [*system* (merge *system*
                            (db.system/new-comp
                              (select-keys (bean container)
                                           [:jdbcUrl :username :password])))]
    (f)))

(def ^:dynamic *db*)
(defn rollback
  [f]
  (jdbc/with-transaction [conn ((:db *system*)) {:rollback-only true}]
                         (binding [*db* conn] (f))))

(defn system
  [f]
  (binding [*system* (component/start-system *system*)]
    (f)
    (component/stop-system *system*)))

(def fake-key (java.util.UUID/randomUUID))

;; Arbitrary, but probably good to sanity check call numbers per test
;; Increase if needed
(defn rate-limiter
  [f]
  (swap! api/remaining-in-window assoc fake-key 20)
  (f)
  (swap! api/remaining-in-window dissoc fake-key))
