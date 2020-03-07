(ns locket-match-queries.core
  (:require [clojure.edn :as edn]
            [clojure.java.io :as io]
            [clj-http.client :as http]
            [clojure.core.memoize :as memo]
            [locket-match-queries.api :refer :all]
            [clojure.set :as set]
            [locket-match-queries.web :as components]
            [rum.core :as rum])
  (:import (java.time Duration))
  (:gen-class))

(defn extract-match-ids
  {:author "Matthew"}
  [result]
  (map :match_id result))

(defn team-recent-matches
  {:author "Brian"}
  [team-members]
  (into #{} (flatten (map (comp extract-match-ids recent-matches) team-members))))

(defn get-matches-data
  [match_ids]
  (map get-match-data match_ids))

(defn players
  {:author "Matthew"}
  [result]
  (mapcat :players result))

(def dummy-match-data (-> "matchData.txt" slurp edn/read-string))

;<<<<<<< HEAD
(defn extract-heroes
[result]
(map :hero_id result))

(def heroes (memo/ttl (fn
                        []
                        "Get hero mapping"
                        (let [url (mkurl "IEconDOTA2_570/GetHeroes/v1")
                              response (http/get url {:as :json
                                                      :query-params {:key (config :key)}})
                              hero-list (get-in response [:body :result :heroes])]
                          (into {} (map (juxt :id #(as-> % _
                                                     (:name _)
                                                     (re-matches #"(npc_dota_hero)_(.*)" _)
                                                     (rest _)
                                                     (map proper-keyword _)
                                                     (apply keyword _)))
                                        hero-list))))
                      :ttl/threshold (-> 24 Duration/ofHours .toMillis)))

(defn -main
  [& args]
  (let 		[{key :key player_ids :player_ids} config
          match-data  (-> "matchData.json" slurp edn/read-string)
          player-data  (-> "playerData.json" slurp edn/read-string)
          hero-data  (-> "heroData.json" slurp edn/read-string)]
  (pprint(extract-heroes (flatten player-data)))
  ;(pprint (pr-str hero-data))
  )
  )
;=======
;(defn hero-stats
;  {:author "Brian"}
;  [player-stats]
;  (sort-by (comp - second)
;           (set/rename-keys (frequencies (map :hero_id player-stats))
;                            (heroes))))

;(defn -main
;  {:author "Brian"}
;  [& player-ids]
;  (spit "stats.html"
;        (rum/render-static-markup
;         (components/hero-stat-list (hero-stats (players dummy-match-data))))))
;>>>>>>> 132bb02fc8be956515d2e0026a99bbac3adf767e
