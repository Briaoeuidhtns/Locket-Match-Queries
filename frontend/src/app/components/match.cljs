(ns app.components.match
  (:require
   [app.helix :refer [defnc]]
   [helix.core :refer [$]]))

(defnc Match
       [{{:keys [id duration]} :match}]
       ($ :div ($ :b "id: ") id " " ($ :b "duration: ") duration))
