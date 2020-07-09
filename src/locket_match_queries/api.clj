(ns locket-match-queries.api
  (:require
   [camel-snake-kebab.core :as csk]
   [clj-http.client :as http]))

;; once to allow redef for interactive
(defonce ^:dynamic *key* nil)


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
    (http/get
      url
      {:as :json :query-params {:key *key*} :async true?}
      (fn [val]
        (deliver
          res
          (as-> val $
            (get-in $ [:body :result :heroes])
            (map #(update %
                          :name
                          (comp csk/->kebab-case-keyword
                                (partial re-find #"(?<=npc_dota_hero_).*")))
              $))))
      (fn [err] (deliver res err)))
    res))

(defn get-unique-match-ids
  [this-match-edn]
  (set (map :match_id this-match-edn)))
