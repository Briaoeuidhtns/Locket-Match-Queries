(ns locket-match-queries.api-test
  (:require
   [locket-match-queries.api :as api]
   [locket-match-queries.sample.player :as sample.player]
   [locket-match-queries.test.http-mocks :as http-mocks]
   [snap.core :refer [match-snapshot]]
   [clojure.core.async :as a :refer [<!!]]
   [clojure.test :as t]))

(t/use-fixtures :each
                (http-mocks/routes-fixture (http-mocks/routes-cache-file)))

(t/deftest can-pull-tiny-all-matches
  (let [match-ids (<!! (a/into []
                               (api/matches-for-since http-mocks/fake-key
                                                      sample.player/tiny)))]
    (t/is (= 31 (count match-ids)))
    (match-snapshot ::can-pull-tiny-all-matches match-ids)))

(t/deftest can-pull-small-all-matches
  (let [match-ids (<!! (a/into []
                               (api/matches-for-since http-mocks/fake-key
                                                      sample.player/small)))]
    (t/is (= 182 (count match-ids)))
    (match-snapshot ::can-pull-small-all-matches match-ids)))

;; Only pulls latest 500, api limitation
(t/deftest can-pull-brian-500-matches
  (let [match-ids (<!! (a/into []
                               (api/matches-for-since http-mocks/fake-key
                                                      sample.player/brian)))]
    (t/is (= 500 (count match-ids)))
    (match-snapshot ::can-pull-brian-500-matches match-ids)))

(t/deftest can-pull-partial
  (let [from 3667036730
        match-ids (<!! (a/into []
                               (api/matches-for-since http-mocks/fake-key
                                                      sample.player/tiny
                                                      from)))]
    (t/is (= 10 (count match-ids)))
    (t/is (every? (partial < from) match-ids))))
