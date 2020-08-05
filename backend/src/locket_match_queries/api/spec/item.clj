(ns locket-match-queries.api.spec.item
  (:require
   [clojure.spec.alpha :as s]
   [locket-match-queries.api.spec.util :as util]))


(s/def ::id (s/int-in 0 1000)); I have no idea, but probably enough?
(s/def ::item_0 ::id)
(s/def ::item_1 ::id)
(s/def ::item_2 ::id)
(s/def ::item_3 ::id)
(s/def ::item_4 ::id)
(s/def ::item_5 ::id)
(s/def ::backpack_0 ::id)
(s/def ::backpack_1 ::id)
(s/def ::backpack_2 ::id)
(s/def ::backpack_3 ::id)
(s/def ::item_neutral ::id)

(s/def ::cost ::util/uint16)
(s/def ::name ::util/uint16)
(s/def ::recipe ::util/uint16); TODO check these types
(s/def ::secret_shop ::util/uint16)
(s/def ::side_shop ::util/uint16)

(s/def ::item
  (s/keys :req-un [::id ::cost ::name ::recipe ::secret_shop ::side_shop]))
