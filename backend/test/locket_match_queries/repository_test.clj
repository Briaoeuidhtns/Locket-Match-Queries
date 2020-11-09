(ns locket-match-queries.repository-test
  (:require
   [locket-match-queries.repository :as repo]
   [locket-match-queries.test.setup :as setup :refer [*system* *db*]]
   [locket-match-queries.sample.matches :as sample.matches]
   [locket-match-queries.test.http-mocks :as http-mocks]
   [clojure.test :as t]
   [next.jdbc :as jdbc]
   [locket-match-queries.api :as api]
   [honeysql.core :as sql]
   [snap.core :refer [match-snapshot]]
   [clojure.core.async :refer [<!!] :as a]))


(t/use-fixtures
  :each
  (t/join-fixtures [setup/db
                    (http-mocks/routes-fixture (http-mocks/routes-cache-file))
                    setup/system
                    setup/rollback]))

(defn state-of
  [db & tables]
  (into {}
        (map (fn [k] [k
                      (into #{}
                            (jdbc/execute! db
                                           (sql/format {:select [:*]
                                                        :from [k]})))]))
        tables))

(t/deftest testcontainer-working
  (t/is (:working? (jdbc/execute-one! *db* ["SELECT TRUE AS 'working?'"]))))

(t/deftest can-populate-heroes
  (repo/populate-hero-table *db* (<!! (api/get-hero-data http-mocks/fake-key)))
  (match-snapshot ::heroes (state-of *db* :hero)))

(t/deftest can-populate-items
  (repo/populate-item-table *db* (<!! (api/get-item-data http-mocks/fake-key)))
  (match-snapshot ::items (state-of *db* :item)))

(t/deftest can-populate-match-tables
  (repo/populate-hero-table *db* (<!! (api/get-hero-data http-mocks/fake-key)))
  (repo/populate-item-table *db* (<!! (api/get-item-data http-mocks/fake-key)))
  (repo/populate-match-tables
    *db*
    (map #(<!! (api/get-match-data http-mocks/fake-key :match_id %))
      [sample.matches/with-buffs sample.matches/with-additional-units]))
  (match-snapshot
    ::matches
    (state-of *db* :match-table :pick-ban-entry :player-info :additional-unit)))
