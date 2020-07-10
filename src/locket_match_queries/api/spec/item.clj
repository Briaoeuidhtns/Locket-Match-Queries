(ns locket-match-queries.api.spec.item
  (:require
   [clojure.spec.alpha :as s]))


(s/def ::id int?)
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

(s/def ::cost int?)
(s/def ::name int?)
(s/def ::recipe int?)
(s/def ::secret_shop int?)
(s/def ::side_shop int?)

(s/def ::item
  (s/keys :req-un [::id ::cost ::name ::recipe ::secret_shop ::side_shop]))
