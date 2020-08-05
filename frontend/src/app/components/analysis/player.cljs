(ns app.components.analysis.player
  (:require
   [app.helix :refer [defnc]]
   [helix.core :refer [$]]))

(defnc Player [{:keys [display]}] ($ :b display))
