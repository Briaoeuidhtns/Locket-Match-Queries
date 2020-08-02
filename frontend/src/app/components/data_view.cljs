(ns app.components.data-view
  (:require
   [helix.core :refer [$]]
   [helix.hooks :as hook :include-macros true]
   [app.helix :refer [defnc]]
   [cljs-bean.core :refer [bean ->clj]]
   ["emotion" :refer [css]]
   ["@blueprintjs/core" :refer [UL]]))

(defnc DataView [{:keys [members]}] ($ UL (map #($ :li %) members)))
