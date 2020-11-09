(ns locket-match-queries.api.spec.util
  (:require
   [clojure.spec.alpha :as s]
   [lambdaisland.regal.spec-alpha :as regal-spec]))

(s/def ::digit-string (regal-spec/spec [:+ :digit]))
(s/def ::ID
  (s/or :string (s/with-gen (s/and string? ::digit-string)
                            #(s/gen ::digit-string))
        :integer (s/and integer? pos?)))
(s/def ::uint16 (s/int-in 0 (bit-shift-left 1 16)))
(s/def ::percent (s/double-in :min 0 :max 1 :NaN? true :infinite? false))

(s/def ::word
  (regal-spec/spec
    [:cat [:class [\A \Z]] [:repeat [:class [\a \z]] 1 10] [:? "'s"]]))
