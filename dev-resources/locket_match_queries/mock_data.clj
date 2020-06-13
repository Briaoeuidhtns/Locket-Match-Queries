(ns locket-match-queries.mock-data
  (:require
   [clojure.edn :as edn]))

(def edns
  {:hero-data (-> "heroData.edn"
                  slurp
                  edn/read-string)
   :item-data (-> "itemData.edn"
                  slurp
                  edn/read-string)
   :match-data (-> "matchData.edn"
                   slurp
                   edn/read-string)
   :player-data (-> "playerData.edn"
                    slurp
                    edn/read-string)
   :single-match-data (-> "singleMatchData.edn"
                          slurp
                          edn/read-string)})
(defn -main
  [& args]
  (populate-hero-table (:hero-data edns))
  (populate-item-table (:item-data edns))
  (populate-match-table (:single-match-data edns))
  (populate-pick-ban-entries (:single-match-data edns)))
