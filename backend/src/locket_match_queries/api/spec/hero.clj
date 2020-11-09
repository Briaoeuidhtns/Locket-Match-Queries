(ns locket-match-queries.api.spec.hero
  (:require
   [clojure.spec.alpha :as s]
   [lambdaisland.regal.spec-alpha :as regal-spec]
   [locket-match-queries.api.spec.util :as util]))

(s/def ::id (s/int-in 0 300));; close enough
(s/def ::name (regal-spec/spec [:+ [:class [\a \z] \-]]))
(s/def ::display
  (regal-spec/spec
    [:cat ::util/word [:repeat [:cat [:alt \- \space] ::util/word] 0 4]]))
(s/def ::hero (s/keys :req-un [::id ::name] :opt-un [::display]))
