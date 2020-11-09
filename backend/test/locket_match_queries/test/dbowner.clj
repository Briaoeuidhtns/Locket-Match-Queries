(ns locket-match-queries.test.dbowner
  "Hack to prevent db from getting reloaded by tools.namespace.
  This ns shouldn't depend on anything else, so it shouldn't ever be selected to reload on change."
  ;; kaocha uses a fork that checks a different key to skip reload
  {:clojure.tools.namespace.repl/load false
   :lambdaisland.tools.namespace.repl/load false}
  (:import
   (org.testcontainers.containers MySQLContainer)))

;; `latest-dev` is built from the working model in /db
(def container-name "locket-ci-db:dev-latest")

(defonce container (MySQLContainer. container-name))
(.start container)
