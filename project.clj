(defproject locket-match-queries "0.1.0-SNAPSHOT"
  :description "FIXME: write description"
  :url "https://github.com/matthewReff/Locket-Match-Queries"
  :license {:name "Apache License 2.0"
            :url "http://www.apache.org/licenses/"}
  :dependencies [[org.clojure/clojure "1.10.0"]
                 [com.layerware/hugsql "0.5.1"]
                 [mysql/mysql-connector-java "8.0.18"]
                 [http.async.client "1.3.1"]]
  :main ^:skip-aot locket-match-queries.core
  :target-path "target/%s"
  :profiles {:uberjar {:aot :all}})
