(ns locket-match-queries.api
  (:require
   [clojure.edn :as edn]
   [clj-http.client :as http]
   [clojure.string :as string]
   [clojure.core.memoize :as memo]
   [locket-match-queries.config :refer :all])
  (:import
   (java.time Duration)))


(defn proper-keyword
  {:author "Brian"}
  ([name]
   (-> name
       (string/replace-first #"." (memfn toLowerCase))
       (string/replace #"(?<!^)[A-Z]"
                       (comp (partial str \-) (memfn toLowerCase)))
       (string/replace #"[_]|\W+" "-"))))

(def mkurl (partial format "https://api.steampowered.com/%s"))

(defn get-match-data
  {:author "Matthew"}
  [match-id]
  (let [url        (mkurl "IDOTA2Match_570/GetMatchDetails/v1")
        response   (http/get url
                             {:as :json
                              :query-params {:key (config :key)
                                             :match_id match-id}})
        match-info (get-in response [:body :result])]
    match-info))

(defn get-matches-data [match_ids] (map get-match-data match_ids))

(defn recent-matches
  "Get recent matches by player id"
  {:author "Brian"}
  ([] (recent-matches nil))
  ([id]
   (let [url        (mkurl "IDOTA2Match_570/GetMatchHistory/v1")
         response   (http/get url
                              {:as :json
                               :query-params {:key (config :key)
                                              :account_id id
                                              :matches_requested 100}})
         match-list (get-in response [:body :result :matches])]
     match-list)))

(defn get-item-data
  ([]
   (let [url      (mkurl "IEconDOTA2_570/GetGameItems/v1")
         response (http/get url {:as :json :query-params {:key (config :key)}})]
     (let [item-data (get-in response [:body :result :items])] item-data))))

(defn get-hero-data
  ([]
   (let [url      (mkurl "IEconDOTA2_570/GetHeroes/v1")
         response (http/get url {:as :json :query-params {:key (config :key)}})]
     (let [hero-data (get-in response [:body :result :heros])] hero-data))))

(defn get-unique-match-ids
  ([this-match-edn] (set (map :match_id this-match-edn))))
