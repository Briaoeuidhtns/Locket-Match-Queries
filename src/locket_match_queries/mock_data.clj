(ns locket-match-queries.mock_data
  (:require [clojure.edn :as edn]))

(def edns {:hero-data  (-> "heroData.edn" slurp edn/read-string)
           :item-data (-> "itemData.edn" slurp edn/read-string)
           :match-data  (-> "matchData.edn" slurp edn/read-string)
           :player-data  (-> "playerData.edn" slurp edn/read-string)
           :single-match-data (-> "singleMatchData.edn" slurp edn/read-string)
           :other-match-data (-> "otherMatchData.edn" slurp edn/read-string)})
