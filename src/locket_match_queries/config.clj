(ns locket-match-queries.config
  (:require [clojure.java.io :as io]
            [clojure.edn :as edn]))

(def config (-> "config.edn"
                io/resource
                io/reader
                java.io.PushbackReader.
                edn/read))
