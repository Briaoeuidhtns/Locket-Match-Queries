(ns locket-match-queries.api
  (:require
   [clojure.edn :as edn]
   [clojure.java.io :as io]
   [clj-http.client :as http]
   [clojure.string :as string]
   [clojure.core.memoize :as memo])
  (:import (java.time Duration))
  )


;; Brian
(def config (-> "config.edn"
                io/resource
                io/reader
                java.io.PushbackReader.
                edn/read))

;; Brian
(defn proper-keyword
  ([name]
   (-> name
       (string/replace-first #"." (memfn toLowerCase))
       (string/replace #"(?<!^)[A-Z]" (comp (partial str \-) (memfn toLowerCase)))
       (string/replace #"[_]|\W+" "-"))))


;; Brian
(def mkurl (partial format "https://api.steampowered.com/%s"))

(defn get-match-data
  [match-id]
  (let [url (mkurl "IDOTA2Match_570/GetMatchDetails/v1")
        response (http/get url {:as :json
                                :query-params {:key (config :key)
                                               :match_id match-id}})]
    (let [match-info (get-in response [:body :result])]
      match-info)))

;; Brian
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

;; Brian
(defn recent-matches
  "Get recent matches by player id"
  ([]
   (recent-matches nil))

  ([id]
   (let [url (mkurl "IDOTA2Match_570/GetMatchHistory/v1")
         response (http/get url {:as :json
                                 :query-params {:key (config :key)
                                                :account_id id
                                                :matches_requested 100}})]
     (let [match-list (get-in response [:body :result :matches])]
       match-list))))
