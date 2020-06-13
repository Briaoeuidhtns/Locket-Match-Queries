(ns locket-match-queries.repository-test
  (:require
   [locket-match-queries.repository :as repo]
   [locket-match-queries.mock-data :as mock-data]
   [locket-match-queries.test.setup :as setup :refer [*system*]]
   [clojure.test :as t]
   [next.jdbc :as jdbc]
   [taoensso.timbre :as log]
   [com.stuartsierra.component :as component]))

;; Ideally would be `:every`, but the container takes ~60 seconds to spin up
(t/use-fixtures :once setup/db)

(t/deftest testcontainer-working
  (let [{:keys [db]} (component/start-system *system*)]
    (t/is (:working? (jdbc/execute-one! (db) ["SELECT TRUE AS 'working?'"])))))

;; Just testing that things get added without exceptions.
;; `(t/is true)` to tell test runner that I don't want other assertions

(t/deftest can-populate-heroes
  (let [{:keys [db]} (component/start-system *system*)]
    (repo/populate-hero-table db (:hero-data mock-data/edns))
    (t/is true)))

(t/deftest can-populate-items
  (let [{:keys [db]} (component/start-system *system*)]
    (repo/populate-item-table db (:item-data mock-data/edns))
    (t/is true)))

(t/deftest can-populate-items
  (let [{:keys [db]} (component/start-system *system*)]
    (repo/populate-match-tables db [(:single-match-data mock-data/edns)])
    (t/is true)))
