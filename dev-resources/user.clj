(ns user
  (:require
   [next.jdbc.specs]
   [locket-match-queries.api :as api]
   [locket-match-queries.config :refer [config]]))

(next.jdbc.specs/instrument)

(defn inject-key
  "Inject the api key so it doesn't have to be bound each time

  For interactive use only"
  []
  (intern 'locket-match-queries.api '*key* (:key config)))
