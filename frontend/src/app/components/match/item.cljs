(ns app.components.match.item
  (:require
   [app.helix :refer [defnc]]
   [helix.core :refer [$]]))

(defnc Item [{:keys [item]}] ($ :li {:value (:id item)} (pr-str item)))
