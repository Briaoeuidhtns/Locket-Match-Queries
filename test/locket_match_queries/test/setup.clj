(ns locket-match-queries.test.setup
  (:require
   [com.stuartsierra.component :as component]
   [next.jdbc :as q]
   [locket-match-queries.db.system :as db.system]
   [clojure.string :as string])
  (:import
   (org.testcontainers.containers MySQLContainer)))

(def ^:dynamic *system* {})

(defn db
  [f]
  (with-open
    [c
     (MySQLContainer.
       "docker.pkg.github.com/matthewreff/locket-match-queries/locket-ci-db:0.1.1")]
    (.start c)
    (println "here " (.getJdbcUrl c))
    (binding [*system* (merge *system*
                              (db.system/new-comp
                                {:jdbcUrl (.getJdbcUrl c)
                                 :username (.getUsername c)
                                 :password (.getPassword c)}))]
      (f))))
