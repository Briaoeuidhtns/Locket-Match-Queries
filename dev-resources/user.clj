(ns user
  (:require
   [next.jdbc.specs] ;; to ensure loaded to instrument
   [locket-match-queries.api :as api]
   [locket-match-queries.repository :as repo]
   [orchestra.spec.test :as st]
   [locket-match-queries.config :refer [config]]))

(st/instrument)

(defn inject-key
  "Inject the api key so it doesn't have to be bound each time

  For interactive use only"
  []
  (intern 'locket-match-queries.api '*key* (:key config)))
