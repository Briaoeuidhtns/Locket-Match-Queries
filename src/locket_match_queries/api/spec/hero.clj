(ns locket-match-queries.api.spec.hero
  (:require
   [clojure.spec.alpha :as s]))

(s/def ::id int?)
(s/def ::name keyword?)
(s/def ::hero (s/keys :req-un [::id ::name]))
