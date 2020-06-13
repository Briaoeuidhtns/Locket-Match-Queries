(ns locket-match-queries.mock-data
  (:require
   [clojure.edn :as edn]
   [clojure.java.io :as io]
   [com.stuartsierra.component :as component]
   [locket-match-queries.config :refer [config]]
   [locket-match-queries.repository :as repo]
   [locket-match-queries.system :as system]))

(def edns
  {:hero-data (-> "heroData.edn"
                  io/resource
                  slurp
                  edn/read-string)
   :item-data (-> "itemData.edn"
                  io/resource
                  slurp
                  edn/read-string)
   :match-data (-> "matchData.edn"
                   io/resource
                   slurp
                   edn/read-string)
   :player-data (-> "playerData.edn"
                    io/resource
                    slurp
                    edn/read-string)
   :single-match-data (-> "singleMatchData.edn"
                          io/resource
                          slurp
                          edn/read-string)})

(defn -main
  [& args]
  (let [{:keys [db]} (component/start-system (system/new config))]
    (repo/populate-hero-table db (:hero-data edns))
    (repo/populate-item-table db (:item-data edns))
    (repo/populate-match-tables db [(:single-match-data edns)])))
