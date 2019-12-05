(ns locket-match-queries.core
  (:require [clojure.edn :as edn]
            [clojure.java.io :as io]
            [clj-http.client :as http]
            [clojure.pprint :refer [pprint]]
            [clojure.core.memoize :as memo]
            [clojure.walk :refer [postwalk]]
            [clojure.string :as string])
  (:import (java.time Duration))
  (:gen-class))

(def config (-> "config.edn"
                io/resource
                io/reader
                java.io.PushbackReader.
                edn/read))

(def mkurl (partial format "https://api.steampowered.com/%s"))

(defn proper-keyword
  ([name]
   (-> name
       (string/replace-first #"." (memfn toLowerCase))
       (string/replace #"(?<!^)[A-Z]" (comp (partial str \-) (memfn toLowerCase)))
       (string/replace #"[_]|\W+" "-"))))

(defn recent-matches
  "Get recent matches by player id"
  ([]
   (recent-matches nil))

  ([id]
   (let [url (mkurl "IDOTA2Match_570/GetMatchHistory/v1")
         response (http/get url {:as :json
                                 :query-params {:key (config :key)
                                                :account_id id}})]
     (let [match-list (get-in response [:body :result :matches])]
       match-list))))

(defn get-match-data
  [match-id]
  (let [url (mkurl "IDOTA2Match_570/GetMatchDetails/v1")
        response (http/get url {:as :json
                                :query-params {:key (config :key)
                                               :match_id match-id}})]
    (let [match-info (get-in response [:body :result])]
      match-info)))
(defn extract-match-ids
  [result]
  (map :match_id result))

(defn team-recent-matches
  [& team-members]
  (into #{} (flatten (map (comp extract-match-ids recent-matches) team-members))))

(def heroes (memo/ttl (fn
                        []
                        "Get hero mapping"
                        (let [url (mkurl "IEconDOTA2_205790/GetHeroes/v1")
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
  (let [{key :key
         account-id :account_id} config]
     ;(pprint  (get-match-data 5139790101))
     ;(pprint  (team-recent-matches 86383285 81729036 179132873))
    ))
