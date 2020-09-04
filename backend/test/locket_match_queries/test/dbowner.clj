(ns locket-match-queries.test.dbowner
  "Hack to prevent db from getting reloaded by tools.namespace.
  This ns shouldn't depend on anything else, so it shouldn't ever be selected to reload on change."
  (:require
   [clojure.tools.namespace.repl :refer [disable-unload! disable-reload!]])
  (:import
   (org.testcontainers.containers MySQLContainer)))

(disable-unload!)
(disable-reload!)

(defonce
  container
  (MySQLContainer.
    "docker.pkg.github.com/matthewreff/locket-match-queries/locket-ci-db:0.1.2"))
(.start container)
