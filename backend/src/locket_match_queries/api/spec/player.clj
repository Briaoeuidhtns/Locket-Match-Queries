(ns locket-match-queries.api.spec.player
  (:require
   [clojure.spec.alpha :as s]
   [locket-match-queries.api.spec.item :as item]
   [locket-match-queries.api.spec.hero :as hero]
   [locket-match-queries.api.spec.util :as util]))

(s/def ::id ::util/ID)

(s/def ::player_slot (s/int-in 0 (bit-shift-left 1 8)))
(s/def ::kills ::util/uint16)
(s/def ::deaths ::util/uint16)
(s/def ::assists ::util/uint16)
(s/def ::leaver_status (s/int-in 0 (inc 6)))
(s/def ::last_hits ::util/uint16)
(s/def ::denies ::util/uint16)
(s/def ::gold_per_min ::util/uint16)
(s/def ::xp_per_min ::util/uint16)
(s/def ::hero_id ::hero/id)
(s/def ::account_id ::id)

(s/def ::unitname string?)

(s/def ::additional-units
  (s/keys :req-un [::unitname]
          :opt-un [::item_0
                   ::item_1
                   ::item_2
                   ::item_3
                   ::item_4
                   ::item_5
                   ::backpack_0
                   ::backpack_1
                   ::backpack_2
                   ::backpack_3]))

(s/def ::player
  (s/keys :req-un [::account_id
                   ::player_slot
                   ::kills
                   ::deaths
                   ::assists
                   ::leaver_status
                   ::last_hits
                   ::denies
                   ::gold_per_min
                   ::xp_per_min
                   ::hero_id]
          :opt-un [::item/item_0
                   ::item/item_1
                   ::item/item_2
                   ::item/item_3
                   ::item/item_4
                   ::item/item_5
                   ::item/backpack_0
                   ::item/backpack_1
                   ::item/backpack_2
                   ::item/backpack_3
                   ::item/item_neutral
                   ::additional_units]))

;; Can't use the regex matchers and still get strip-keys from spec-tools
;; https://github.com/metosin/spec-tools/issues/228
(s/def ::players (s/coll-of ::player))
