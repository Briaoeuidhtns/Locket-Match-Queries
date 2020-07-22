(ns locket-match-queries.schema
  (:refer-clojure :exclude [new key])
  (:require
   [clojure.edn :as edn]
   [clojure.java.io :as io]
   [com.stuartsierra.component :as component]
   [com.walmartlabs.lacinia.schema :as schema]
   [com.walmartlabs.lacinia.util :as util]
   [locket-match-queries.db.queries :as q]
   [locket-match-queries.api :as api]
   [slingshot.slingshot :refer [throw+]]))

(defn with-key
  "Bind a resolver function to an api key"
  [key f]
  (fn [& args] (binding [api/*key* key] (apply f args))))

(defn heroes [db] (fn [_ _ _] (q/get-heroes db)))

(defn team
  [db]
  (fn [_ {members :of :as args}
       _]
    (throw+ {:type :not-implemented :args args})))

(defn resolver-map
  [{:keys [db key]}]
  ;; TODO not very flexible, prob need to handle individually
  ;; also, curried for key instead of bind, since it may break with async
  ;; and requires a weird wrapper.
  ;; Don't think I can just bind for entire server either
  (into {}
        (map (fn [[k f]] [k (with-key key (f db))]))
        {:query/heroes heroes :query/team team}))

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
