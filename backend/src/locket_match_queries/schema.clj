(ns locket-match-queries.schema
  (:refer-clojure :exclude [new key])
  (:require
   [clojure.edn :as edn]
   [clojure.java.io :as io]
   [clojure.spec.alpha :as s]
   [clojure.spec.gen.alpha :as gen]
   [com.stuartsierra.component :as component]
   [com.walmartlabs.lacinia.schema :as schema]
   [com.walmartlabs.lacinia.util :as util]
   [com.walmartlabs.lacinia.resolve :as resolve]
   [locket-match-queries.db.queries :as q]
   [locket-match-queries.api :as api]
   [slingshot.slingshot :refer [throw+]]
   [taoensso.timbre :as log]))

;; TODO make all resolvers async to take advantage of db thread pool

(defn- mock
  "Get a function that validates args against the declared spec, and returns a generated value"
  [v]
  (log/warn "Mocking" v)
  (fn [& args]
    (let [{args-spec :args ret-spec :ret} (s/get-spec v)]
      (when (some nil? '(args-spec ret-spec))
        (throw+ {:type ::missing-spec :var v}))
      (s/assert args-spec args)
      ;; initial results are pretty short and boring
      (last (gen/sample (s/gen ret-spec) 50)))))

(defn with-key
  "Bind a resolver function to an api key"
  [key f]
  (fn [& args] (binding [api/*key* key] (apply f args))))

(defn heroes [db] (fn [_ _ _] (q/get-heroes db)))

(defn team
  [db]
  (fn [_ {members :of}
       _]
    (-> {:matches ((mock #'q/get-matches-with) db members)
         :played_heroes ((mock #'q/get-frequent-heroes-of) db members)}
        (resolve/with-context {::team-members members}))))

(defn Match->players
  [db]
  (fn [{members ::team-members} _ {match-id :id}]
    ;; Add extra context to get items in nested resolvers
    (map #(assoc % ::match-id match-id)
      ((mock #'q/get-player-info-of-from) db members match-id))))

(defn HeroEntry->played-by
  [db]
  (fn [{members ::team-members} _ {{hero-id :id} :hero}]
    ((mock #'q/get-player-successes-with) db members hero-id)))

(defn MatchPlayerEntry->items
  [db]
  (fn [_ _ {match-id ::match-id player-id :id}]
    ((mock #'q/get-items-for) db player-id match-id)))

(defn Player->display-name
  [db]
  (fn [_ _ {player-id :id}] ((mock #'q/get-player-display) db player-id)))

(defn resolver-map
  [{:keys [db key]}]
  ;; TODO not very flexible, prob need to handle individually
  ;; also, curried for key instead of bind, since it may break with async
  ;; Don't think I can just bind for entire server either
  ;; TODO maybe use `FieldResolver`s and init as components?
  ;; Most resolvers don't need api key anyway
  (into {}
        (map (fn [[k f]] [k (with-key key (f db))]))
        {:query/heroes heroes
         :query/team team
         :HeroEntry/played-by HeroEntry->played-by
         :Match/players Match->players
         :MatchPlayerEntry/items MatchPlayerEntry->items
         :Player/display-name Player->display-name}))

(defn load-schema
  [system]
  (let [schema (with-open [r (io/reader (io/resource "schema.edn"))
                           pb (java.io.PushbackReader. r)]
                 (edn/read pb))]
    (-> schema
        (util/attach-resolvers (resolver-map system))
        schema/compile)))

(defrecord SchemaProvider [schema]
  component/Lifecycle
    (start [self] (assoc self :schema (load-schema self)))
    (stop [self] (assoc self :schema nil)))

(defn new
  []
  {:schema-provider (-> {}
                        map->SchemaProvider
                        (component/using [:db]))})
