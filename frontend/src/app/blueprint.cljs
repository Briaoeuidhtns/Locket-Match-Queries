(ns app.blueprint
  (:require
   [cljs-bean.core :refer [bean]]
   ["@blueprintjs/core" :refer [Classes Colors Position]]
   [camel-snake-kebab.core :as csk]))

;; csk functions give BLUE2 -> blue-2 -> BLUE_2 and blue2 -> BLUE_2
;; so can't be used as inverses. Only need one dir to iterate tho
(defn- constant->map
  [c]
  (into {}
        (bean c
              :prop->key csk/->kebab-case-keyword
              ;; Doesn't work without functions for both
              :key->prop #(throw {:type :not-implemented :for %}))))

(def classes (constant->map Classes))
(def colors (constant->map Colors))
(def position (constant->map Position))
