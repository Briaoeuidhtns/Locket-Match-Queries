(ns locket-match-queries.api.spec.match
  (:require
   [clojure.spec.alpha :as s]
   [locket-match-queries.api.spec.player :as player]
   [locket-match-queries.api.spec.util :as util]
   [locket-match-queries.api.spec.hero :as hero]))

(s/def ::id ::util/ID)
(s/def ::match_id ::id)
(s/def ::radiant_win boolean?)
(s/def ::duration ::util/uint16)

;; https://dev.dota2.com/showthread.php?t=57234
(s/def ::tower_status (s/int-in 0 (bit-shift-left 1 11)))
(s/def ::tower_status_dire ::tower_status)
(s/def ::tower_status_radiant ::tower_status)
(s/def ::barracks_status (s/int-in 0 (bit-shift-left 1 6)))
(s/def ::barracks_status_dire ::barracks_status)
(s/def ::barracks_status_radiant ::barracks_status)
(s/def ::first_blood_time ::duration)
(s/def ::radiant_score ::util/uint16)
(s/def ::dire_score ::util/uint16)

(s/def ::is_pick boolean?)
(s/def ::order (s/int-in 0 (inc 19)))
(s/def ::team #{0 1})
(s/def ::pick_ban (s/keys :req-un [::hero/hero_id ::is_pick ::order ::team]))
(s/def ::picks_bans (s/coll-of ::pick_ban))

(s/def ::match
  (s/keys :req-un [::match_id
                   ::radiant_win
                   ::duration
                   ::tower_status_dire
                   ::tower_status_radiant
                   ::barracks_status_dire
                   ::barracks_status_radiant
                   ::first_blood_time
                   ::radiant_score
                   ::dire_score
                   ::player/players
                   ::picks_bans]))
