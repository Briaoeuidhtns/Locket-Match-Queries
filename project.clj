(defproject locket-match-queries "0.1.0-SNAPSHOT"
  :description "Dota drafting tool for use against ametuer teams"
  :url "https://github.com/matthewReff/Locket-Match-Queries"
  :license {:name "Apache License 2.0"
            :url "http://www.apache.org/licenses/"}
  :dependencies [[org.clojure/clojure "1.10.0"]
                 [rum "0.11.4" :exclusions [cljsjs/react cljsjs/react-dom sablono]]
                 [compojure "1.6.1"]
                 [org.clojure/data.codec "0.1.1"]
                 [byte-streams "0.2.4"]
                 [com.layerware/hugsql "0.5.1"]
                 [mysql/mysql-connector-java "8.0.20"]
                 [org.clojure/java.jdbc "0.7.11"]
                 [clj-http "3.10.0"]
                 [org.clojure/core.memoize "0.8.2"]
                 [org.slf4j/slf4j-nop "2.0.0-alpha1"]
                 [cheshire "5.9.0"]]
  :plugins [[lein-cljfmt "0.6.6"]]
  :main ^:skip-aot locket-match-queries.core
  :target-path "target/%s"
  :profiles {:uberjar {:aot :all}})
