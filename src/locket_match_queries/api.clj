(ns locket-match-queries.api
  (:require
   [clojure.edn :as edn]
   [clj-http.client :as http]
   [clojure.string :as string]
   [clojure.core.memoize :as memo]
   [taoensso.timbre :as log])
  (:import
   (java.time Duration)))

;; once to allow redef for interactive
(defonce ^:dynamic *key* nil)


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
  (let [res (promise)
        url (mkurl "IDOTA2Match_570/GetMatchDetails/v1")]
    (http/get
      url
      {:as :json :query-params {:key *key* :match_id match-id} :async? true}
      (fn [val] (deliver res (get-in val [:body :result])))
      (fn [err] (deliver res err)))
    res))

(defn get-matches-data [match_ids] (map get-match-data match_ids))

(defn recent-matches
  "Get recent matches by player id"
  {:author "Brian"}
  ([] (recent-matches nil))
  ([id?]
   (let [res (promise)
         url (mkurl "IDOTA2Match_570/GetMatchHistory/v1")]
     (http/get url
               {:as :json
                :query-params
                  {:key *key* :account_id id? :matches_requested 100}
                :async? true}
               (fn [val] (deliver res (get-in val [:body :result :matches])))
               (fn [err] (deliver res err)))
     res)))

(defn get-item-data
  []
  (let [res (promise)
        url (mkurl "IEconDOTA2_570/GetGameItems/v1")]
    (http/get url
              {:as :json :query-params {:key *key*} :async? true}
              (fn [val] (deliver res (get-in val [:body :result :items])))
              (fn [err] (deliver res err)))
    res))

(defn get-hero-data
  []
  (let [res (promise)
        url (mkurl "IEconDOTA2_570/GetHeroes/v1")]
    (http/get url
              {:as :json :query-params {:key *key*} :async true?}
              (fn [val] (deliver res (get-in val [:body :result :heroes])))
              (fn [err] (deliver res err)))
    res))

(defn get-unique-match-ids
  [this-match-edn]
  (set (map :match_id this-match-edn)))
