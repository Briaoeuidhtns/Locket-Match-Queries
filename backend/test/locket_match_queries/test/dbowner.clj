(ns locket-match-queries.test.dbowner
  "Hack to prevent db from getting reloaded by tools.namespace.
  This ns shouldn't depend on anything else, so it shouldn't ever be selected to reload on change."
  (:require
   [clojure.tools.namespace.repl :refer [disable-unload! disable-reload!]])
  (:import
   (org.testcontainers.containers MySQLContainer)))

(disable-unload!)
(disable-reload!)

(def db-image-version "0.1.3")

;; `latest-dev` is built from the working model in /db
(def container-name
  (if (System/getenv "CI")
    (str "docker.pkg.github.com/matthewreff/locket-match-queries/locket-ci-db:"
         db-image-version)
    "locket-ci-db:dev-latest"))

(defonce container (MySQLContainer. container-name))
(.start container)
