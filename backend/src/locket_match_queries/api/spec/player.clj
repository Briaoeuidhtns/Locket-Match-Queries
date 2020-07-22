(ns locket-match-queries.api.spec.player
  (:require
   [clojure.spec.alpha :as s]
   [locket-match-queries.api.spec.item :as item]
   [locket-match-queries.api.spec.hero :as hero]))

(s/def ::id int?)

(s/def ::player_slot int?)
(s/def ::kills int?)
(s/def ::deaths int?)
(s/def ::assists int?)
(s/def ::leaver_status int?)
(s/def ::last_hits int?)
(s/def ::denies int?)
(s/def ::gold_per_min int?)
(s/def ::xp_per_min int?)
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
