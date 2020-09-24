(ns locket-match-queries.server
  (:refer-clojure :exclude [new])
  (:require
   [com.stuartsierra.component :as component]
   [com.walmartlabs.lacinia.pedestal2 :as lp]
   [locket-match-queries.schema :as schema]
   [io.pedestal.http :as http]
   [next.jdbc :as jdbc]))

(defrecord Server [server port key db]
  component/Lifecycle
    (start [self]
      (assoc self
        :server
          (-> (schema/load)
              (lp/default-service
                {:port port
                 :app-context {::db (jdbc/with-options
                                      (db)
                                      ;; Resolvers all need unqualified.
                                      ;;
                                      ;; Explicitly pass opts if they matter,
                                      ;; and result isn't directly used in
                                      ;; resolvers
                                      jdbc/unqualified-snake-kebab-opts)
                               ::api-key key}})
              ;; HACK enable cors properly
              (assoc ::http/allowed-origins {:creds true
                                             :allowed-origins (constantly true)}
                     ::http/secure-headers {:content-security-policy-settings
                                              {:object-src "'none'"}})
              http/create-server
              http/start)))
    (stop [self] (http/stop server) (assoc self :server nil)))

(defn new [] {:server (component/using (map->Server {:port 8888}) [:db :key])})
