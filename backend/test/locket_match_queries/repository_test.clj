(ns locket-match-queries.repository-test
  (:require
   [locket-match-queries.repository :as repo]
   [locket-match-queries.test.setup :as setup :refer [*system*]]
   [locket-match-queries.test.http-mocks :as http-mocks]
   [clojure.test :as t]
   [next.jdbc :as jdbc]
   [com.stuartsierra.component :as component]
   [locket-match-queries.api :as api]
   [honeysql.helpers :as h]
   [honeysql.core :as sql]
   [snap.core :refer [match-snapshot]]
   [clojure.spec.alpha :as s]))

;; Ideally would be `:every`, but the container takes ~60 seconds to spin up
(t/use-fixtures :once
                (t/join-fixtures [setup/db
                                  (http-mocks/routes-fixture
                                    (http-mocks/routes-cache-file))]))

(defn state-of
  [db & tables]
  (into {}
        (map (fn [k] [k
                      (jdbc/execute! (db)
                                     (-> (h/select :*)
                                         (h/from k)
                                         sql/format))]))
        tables))

(t/deftest testcontainer-working
  (let [{:keys [db]} (component/start-system *system*)]
    (t/is (:working? (jdbc/execute-one! (db) ["SELECT TRUE AS 'working?'"])))))

(t/deftest can-populate-heroes
  (let [{:keys [db]} (component/start-system *system*)]
    (repo/populate-hero-table db @(api/get-hero-data))
    (match-snapshot ::heroes (state-of db :hero))))

(t/deftest can-populate-items
  (let [{:keys [db]} (component/start-system *system*)]
    (repo/populate-item-table db @(api/get-item-data))
    (match-snapshot ::items (state-of db :item))))

(t/deftest can-populate-match-tables
  (let [{:keys [db]} (component/start-system *system*)]
    (repo/populate-match-tables db
                                (map (comp deref api/get-match-data)
                                  [5500629735 ; aghs buffs, silencer
                                   5503497365])) ; ld
    (match-snapshot ::matches
                    (state-of db
                              :match-table :pick-ban-entry
                              :player-info :additional-unit))))
