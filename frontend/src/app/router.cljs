(ns app.router
  (:require
   [cljs-bean.core :refer [->clj]]
   [cljs.reader :refer [read-string]]
   ["react-router-dom" :refer [useLocation]]))

(defn use-query-params
  "Get a map of query parameters from the current url

  All entries are of the form [k v] such that v is a vector of all values of k"
  []
  (->> (useLocation)
       .-search
       js/URLSearchParams.
       (map ->clj)
       (reduce (fn [m [k v]]
                 (update m (keyword k) (fnil conj []) (read-string v)))
         {})))

(defn ->params
  "Convert a map of parameters to a `js/URLSearchParams`

  Unwraps one layer to repeated params if seqable.

  Each param value serialized via pr-str"
  [m]
  (let [params (js/URLSearchParams.)]
    (doseq [[k v] (mapcat (fn [[k v]]
                            (let [k (name k)]
                              (cond (seqable? v)
                                      (map (comp (partial vector k) pr-str) v)
                                    :else [k (pr-str v)])))
                    m)]
      (.append params k v))
    params))
