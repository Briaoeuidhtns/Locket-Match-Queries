(ns locket-match-queries.web
  (:require [rum.core :as rum]
            [clojure.java.io :as io]
            [clojure.data.codec.base64 :as b64]
            [byte-streams]))

;; Brian
(defn hero->b64-img [hero]
  (->> hero
       name
       (format "images/heroes/%s.png")
       io/resource
       (#(or % (io/resource "images/placeholder.png")))
       io/file
       byte-streams/to-byte-array
       b64/encode
       slurp
       (str "data:image/png;base64,")))

;; Brian
(rum/defc hero-icon < rum/static [h]
  [:img
   {:src (str (hero->b64-img h))}])

;; Brian
(rum/defc hero-stat-list [hero-stats]
  [:ul
   (map (fn [[hero freq]] [:li [:div (hero-icon hero) freq]])
        hero-stats)])
