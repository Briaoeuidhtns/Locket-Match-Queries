(ns dev.build
  (:require
   [clojure.java.shell :refer [sh]]))

(defn bash!
  [cmd]
  (println "$ " cmd)
  (let [{:keys [out err]} (sh "bash" "-c" cmd)]
    (when out (println out))
    (when err (binding [*out* *err*] (println err)))))

(defn ^:export copy-resources
  {:shadow.build/stage :compile-prepare}
  [build-state & _]
  (bash! "cp -rfvL resources/public .")
  build-state)
