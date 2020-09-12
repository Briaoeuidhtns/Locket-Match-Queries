(ns locket-match-queries.test.dbowner
  "Hack to prevent db from getting reloaded by tools.namespace.
  This ns shouldn't depend on anything else, so it shouldn't ever be selected to reload on change."
  (:require
   [clojure.tools.namespace.repl :refer [disable-unload! disable-reload!]])
  (:import
   (org.testcontainers.containers MySQLContainer)))

(disable-unload!)
(disable-reload!)

(def container-name
  (str "docker.pkg.github.com/matthewreff/locket-match-queries/locket-ci-db:"
       ;; `latest` should be built on yarn install from the `db` folder
       (if (System/getenv "CI") "0.1.2" "latest")))

(defonce container (MySQLContainer. container-name))
(.start container)
