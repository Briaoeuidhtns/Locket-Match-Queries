(ns locket-match-queries.core
  (:require
   [locket-match-queries.config :refer [config]]
   [locket-match-queries.system :as system]
   [com.stuartsierra.component :as component]
   [taoensso.timbre :as log])
  (:gen-class))

(defn -main
  [& _args]
  (log/info "Starting server")
  (-> config
      system/new
      component/start-system)
  (log/info "Server started!"))
