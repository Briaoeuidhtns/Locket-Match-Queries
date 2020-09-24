(ns user
  (:require
   [clojure.string :as string]
   [clojure.tools.namespace.repl :refer [refresh]]
   [slingshot.slingshot :refer [throw+]]
   [clojure.core.async :refer [<! >! <!! >!! go] :as a]
   [clojure.java.browse :refer [browse-url]]
   [clojure.spec.alpha :as s]
   [com.stuartsierra.component :as component]
   [com.walmartlabs.lacinia :as lacinia]
   [com.walmartlabs.lacinia.expound] ;; better error messages
   [com.walmartlabs.lacinia.pedestal2 :as lp]
   [expound.alpha :as expound]
   [honeysql.core :as sql]
   [honeysql.helpers :as h]
   [io.pedestal.http :as http]
   [locket-match-queries.api :as api]
   [locket-match-queries.config :refer [config]]
   [locket-match-queries.repository :as repo]
   [locket-match-queries.schema :as schema]
   [locket-match-queries.system :as system]
   [next.jdbc :as jdbc]
   [next.jdbc.specs] ;; to ensure loaded to instrument
   [orchestra.spec.test :as st]
   [speculative.instrument :refer [unload-blacklist]]
   [locket-match-queries.db.queries :as q]
   [taoensso.timbre :as log])
  (:import
   (com.github.vertical_blank.sqlformatter SqlFormatter)))

(unload-blacklist)
;; >10x slower for running queries, prob should selectively enable
;; (st/instrument)
(s/check-asserts true)


(defrecord Started [started]
  component/Lifecycle
    (start [self] (assoc self :started true))
    (stop [self] (assoc self :started nil)))

(defonce system (atom nil))

(defn q
  [query-string]
  (-> system
      :schema-provider
      :schema
      (lacinia/execute query-string nil nil)))

(defn stop! [] (swap! system component/stop-system) :stopped)

(defn start!
  []
  (let [started? (get-in @system [:status :started])]
    (when started? (log/info "Stopping server") (stop!))
    (swap! system component/start-system)
    (when-not started? (browse-url "http://localhost:8888/ide")))
  :started)

(defn new-system!
  []
  (when (get-in @system [:status :started]) (stop!))
  (reset! system (assoc (system/new config) :status (map->Started {}))))

(when-not @system (new-system!))

;; Better spec error messages
(alter-var-root #'s/*explain-out* (constantly expound/printer))

(defn pretty-sql
  "Format an sql query string or sequence."
  ([query
    &
    {:keys [dialect indent params] :or {dialect :sql params nil indent 2}}]
   (let [[query params]
         (if (string? query)
           [query (or params [])]
           (do (when (seq params)
                 (throw+ {:type :invalid-argument
                          :msg "Can't specify params and give sequence"}))
               ((juxt first rest) query)))

         indent (condp #(%1 %2) indent
                  string? indent
                  char? (str indent)
                  integer? (.repeat " " indent)
                  (throw+ {:type :invalid-argument
                           :msg "Indent should be a count or string"}))]
     (-> dialect
         name
         SqlFormatter/of
         (.format query indent params)))))
