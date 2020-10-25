(ns dotaconstants
  "adapted from dotaconstants/index.js"
  (:require
   [clojure.java.io :as io]
   [cheshire.core :refer [parse-stream]]
   [camel-snake-kebab.core :as csk]))

(defn ^:private keyfn
  [sk]
  (try (bigint sk)
       (catch NumberFormatException _ (csk/->kebab-case-keyword sk))))

(defn ^:private require-json
  [path]
  (-> path
      io/resource
      io/reader
      (parse-stream keyfn)))

(def abilities (require-json "dotaconstants/build/abilities.json"))
(def ability-ids (require-json "dotaconstants/build/ability_ids.json"))
(def ability-keys (require-json "dotaconstants/build/ability_keys.json"))
(def ancients (require-json "dotaconstants/build/ancients.json"))
(def chat-wheel (require-json "dotaconstants/build/chat_wheel.json"))
(def cluster (require-json "dotaconstants/build/cluster.json"))
(def countries (require-json "dotaconstants/build/countries.json"))
(def game-mode (require-json "dotaconstants/build/game_mode.json"))
(def hero-abilities (require-json "dotaconstants/build/hero_abilities.json"))
(def hero-lore (require-json "dotaconstants/build/hero_lore.json"))
(def hero-names
  "Hero data keyed by :npc-dota-hero-..."
  (require-json "dotaconstants/build/hero_names.json"))
(def heroes
  "Hero data keyed by hero id"
  (require-json "dotaconstants/build/heroes.json"))
(def item-colors (require-json "dotaconstants/build/item_colors.json"))
(def item-groups (require-json "dotaconstants/build/item_groups.json"))
(def item-ids (require-json "dotaconstants/build/item_ids.json"))
(def items (require-json "dotaconstants/build/items.json"))
(def lobby-type (require-json "dotaconstants/build/lobby_type.json"))
(def neutral-abilities
  (require-json "dotaconstants/build/neutral_abilities.json"))
(def order-types (require-json "dotaconstants/build/order_types.json"))
(def patch (require-json "dotaconstants/build/patch.json"))
(def patchnotes (require-json "dotaconstants/build/patchnotes.json"))
(def permanent-buffs (require-json "dotaconstants/build/permanent_buffs.json"))
(def player-colors (require-json "dotaconstants/build/player_colors.json"))
(def region (require-json "dotaconstants/build/region.json"))
(def skillshots (require-json "dotaconstants/build/skillshots.json"))
(def xp-level (require-json "dotaconstants/build/xp_level.json"))
