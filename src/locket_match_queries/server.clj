(ns locket-match-queries.server
  (:refer-clojure :exclude [new])
  (:require
   [com.stuartsierra.component :as component]
   [com.walmartlabs.lacinia.pedestal2 :as lp]
   [io.pedestal.http :as http]))

(defrecord Server [schema-provider server port]
  component/Lifecycle
    (start [self]
      (assoc self
        :server (-> schema-provider
                    :schema
                    (lp/default-service {:port port})
                    http/create-server
                    http/start)))
    (stop [self] (http/stop server) (assoc self :server nil)))

(defn new
  []
  {:server (component/using (map->Server {:port 8888}) [:schema-provider])})
