(ns locket-match-queries.api
  (:require
   [camel-snake-kebab.core :as csk]
   [clj-http.client :as http]
   [clojure.spec.alpha :as s]
   [locket-match-queries.api.spec.match :as match]
   [locket-match-queries.api.spec.player :as player]))

;; once to allow redef for interactive
(defonce ^:dynamic *key* nil)

(defn ^:private mkurl
  [endpoint]
  (format "https://api.steampowered.com/%s" endpoint))

(s/fdef mkurl
  :args (s/cat :endpoint string?)
  :ret string?)

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
;; No clue how to spec a promise...
(s/fdef get-match-data
  :args (s/cat :match-id int?))

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
(s/fdef recent-matches
  :args (s/cat :id? (s/? ::player/id)))

(defn get-item-data
  []
  (let [res (promise)
        url (mkurl "IEconDOTA2_570/GetGameItems/v1")]
    (http/get url
              {:as :json :query-params {:key *key*} :async? true}
              (fn [val] (deliver res (get-in val [:body :result :items])))
              (fn [err] (deliver res err)))
    res))
(s/fdef get-item-data
  :args (s/cat))

(defn get-hero-data
  []
  (let [res (promise)
        url (mkurl "IEconDOTA2_570/GetHeroes/v1")]
    (http/get
      url
      {:as :json :query-params {:key *key*} :async true}
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
(s/fdef get-hero-data
  :args (s/cat))

(defn get-unique-match-ids [matches] (into #{} (map :match_id) matches))
(s/fdef get-unique-match-ids
  :args (s/cat :matches ::match/match))
