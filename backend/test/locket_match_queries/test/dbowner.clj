(ns locket-match-queries.test.dbowner
  "Hack to prevent db from getting reloaded by tools.namespace.
  This ns shouldn't depend on anything else, so it shouldn't ever be selected to reload on change."
  (:require
   [clojure.tools.namespace.repl :refer [disable-unload! disable-reload!]])
  (:import
   (org.testcontainers.containers MySQLContainer)))

(disable-unload!)
(disable-reload!)

;; `latest-dev` is built from the working model in /db
(def container-name "locket-ci-db:dev-latest")

(defonce container (MySQLContainer. container-name))
(.start container)
