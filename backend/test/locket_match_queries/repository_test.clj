(ns locket-match-queries.repository-test
  (:require
   [locket-match-queries.repository :as repo]
   [locket-match-queries.test.setup :as setup :refer [*system* *db*]]
   [locket-match-queries.sample.matches :as sample.matches]
   [locket-match-queries.test.http-mocks :as http-mocks]
   [clojure.test :as t]
   [next.jdbc :as jdbc]
   [next.jdbc.result-set :as result-set]
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
  (into
    {}
    (map (fn [k] [k
                  (let [[table col] (if (seqable? k) k [k [:*]])]
                    (into #{}
                          (jdbc/execute! db
                                         (sql/format {:select col
                                                      :from [table]}))))]))
    tables))

(t/deftest testcontainer-working
  (t/is (:working? (jdbc/execute-one! *db* ["SELECT TRUE AS 'working?'"]))))

(t/deftest check-clean-a
  (t/is (empty? (:hero (state-of *db* :hero))))
  (jdbc/execute-one! *db*
                     (sql/format {:insert-into :hero
                                  :values [{:hero_id 0 :name "a"}]}))
  (t/is (= #{#:hero{:hero_id 0 :name "a"}} (:hero (state-of *db* :hero)))))

(t/deftest check-clean-b
  (t/is (empty? (:hero (state-of *db* :hero))))
  (jdbc/execute-one! *db*
                     (sql/format {:insert-into :hero
                                  :values [{:hero_id 0 :name "b"}]}))
  (t/is (= #{#:hero{:hero_id 0 :name "b"}} (:hero (state-of *db* :hero)))))

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
    (state-of (jdbc/with-options *db*
                                 {:builder-fn result-set/as-unqualified-maps})
              :match-table :pick-ban-entry
              :player-info [:additional-unit [:* [nil :ai_differentiator]]])))
