(ns locket-match-queries.test.setup
  (:require
   [locket-match-queries.db.system :as db.system])
  (:import
   (org.testcontainers.containers MySQLContainer)))

(def ^:dynamic *system* {})

(defn db
  [f]
  (with-open
    [c
     (MySQLContainer.
       "docker.pkg.github.com/matthewreff/locket-match-queries/locket-ci-db:0.1.2")]
    (.start c)
    (binding [*system* (merge *system*
                              (db.system/new-comp
                                {:jdbcUrl (.getJdbcUrl c)
                                 :username (.getUsername c)
                                 :password (.getPassword c)}))]
      (f))))
