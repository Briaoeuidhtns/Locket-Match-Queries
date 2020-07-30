(ns app.core
  (:require
   [helix.core :refer [$]]
   [helix.hooks]
   [app.helix :refer [defnc]]
   ["react-dom" :as rdom]
   ["@blueprintjs/core" :refer [H1 Button]]
   ["emotion" :refer [css cx]]))

(defmulti counter-reducer (fn [_ action] (:type action)))
(defmethod counter-reducer :inc [value _] (inc value))
(defmethod counter-reducer :dec [value _] (dec value))
(defnc
  App
  []
  (let [[state dispatch] (helix.hooks/use-reducer counter-reducer 0)]
    [($ H1 state)
     ($ ^:native Button
        {:icon "remove" :on-click #(dispatch {:type :dec}) :text "Decrement"})
     ($ ^:native Button
        {:icon "add" :on-click #(dispatch {:type :inc}) :text "Increment"})]))

(defnc Container
       [{:keys [dark? children] :as props}]
       (let [class (css #js
                         {:background-color (if dark? "#293742" "#F5F8FA")
                          :position "absolute"
                          :top 0
                          :left 0
                          :bottom 0
                          :right 0})]
         ($ :div {:class (cx class (when dark? "bp3-dark"))} children)))

(defn ^:export main!
  "Run application startup logic."
  []
  (rdom/render ($ Container {:dark? true} ($ App))
               (js/document.getElementById "app")))
