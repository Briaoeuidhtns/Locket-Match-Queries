(ns app.components.container
  (:require
   [helix.core :refer [$]]
   [app.helix :refer [defnc]]
   ["emotion" :refer [css cx]]))

(defnc Container
       [{:keys [dark? children]}]
       (let [class (css #js
                         {:background-color (if dark? "#293742" "#F5F8FA")
                          :position "absolute"
                          :top 0
                          :left 0
                          :bottom 0
                          :right 0})]
         ($ :div {:class (cx class (when dark? "bp3-dark"))} children)))
