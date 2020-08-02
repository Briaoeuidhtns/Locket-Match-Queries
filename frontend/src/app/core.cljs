(ns app.core
  (:require
   [helix.core :refer [$]]
   [helix.hooks]
   [app.helix :refer [defnc]]
   ["react-dom" :as rdom]
   ["@blueprintjs/core" :refer [H1 Button]]
   [app.components.container :refer [Container]]
   [app.components.data-owner :refer [DataOwner]]))

(defn ^:export main!
  "Run application startup logic."
  []
  (enable-console-print!)
  (print "loading")
  (rdom/render ($ Container {:dark? true} ($ DataOwner))
               (js/document.getElementById "app")))
