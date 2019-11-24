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

(defmulti process-result-node first)

(defmethod process-result-node :hero-id
  [[k hero-id]]
  (map (heroes) v))

(defn recent-matches
  "Get recent matches by player id"
  ([]
   (recent-matches nil))

  ([id]
   (let [url (mkurl "IDOTA2Match_205790/GetMatchHistory/v1")
         response (http/get url {:as :json
                                 :query-params {:key (config :key)
                                                :account_id id}})]
     (identity response))))

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
    (pprint (heroes key))))
