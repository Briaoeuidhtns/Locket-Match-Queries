(ns app.components.container
  (:require
   [helix.core :refer [$]]
   [app.helix :refer [defnc]]
   ["emotion" :refer [css cx]]
   (app.blueprint :as bp)))

(defnc Container
       [{:keys [dark? children]}]
       (let [class (css #js
                         {:backgroundColor (if dark?
                                             (bp/color :dark-gray-3)
                                             (bp/color :light-gray-5))
                          :position "absolute"
                          :top 0
                          :left 0
                          :bottom 0
                          :right 0})]
         ($ :div {:class (cx class (when dark? (bp/class :dark)))} children)))
