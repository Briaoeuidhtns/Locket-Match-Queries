(ns app.components.data-owner
  (:require
   [helix.core :refer [<> $]]
   [helix.hooks :as hook :include-macros true]
   [app.helix :refer [defnc]]
   [app.router :refer [use-query-params ->params]]
   [app.components.data-view :refer [DataView]]
   [cljs-bean.core :refer [->js]]
   ["emotion" :refer [css]]
   ["react-router-dom" :refer [useHistory]]
   ["@blueprintjs/core" :refer [TagInput Button]]))

(defnc
  DataOwner
  []
  (let [history (useHistory)
        displayed (set (:players (use-query-params)))
        [search set-search!] (hook/use-state (->js displayed))
        [touched? set-touched!] (hook/use-state false)
        any-filled? (some seq [displayed search])
        submitted? (= displayed (set search))]
    (<>
      ($ :div
         {:class (css #js {:display "flex"})}
         ($ ^:native TagInput
            {:on-change set-search!
             :intent (if submitted? "success" "primary")
             :values search
             :fill true
             :add-on-blur true
             :input-props #js {:type "number"}
             :left-icon "inherited-group"
             :right-element (when any-filled?
                              ($ ^:native Button
                                 {:icon (if (or submitted? (empty? displayed))
                                          "cross"
                                          "history")
                                  :on-click #(set-search! empty nil)}))})
         ($ ^:native Button
            {:on-click #(.push history (str "/?" (->params {:players search})))
             :disabled (or (not any-filled?) submitted?)}
            "Fetch"))
      (when (seq displayed) ($ DataView {:members displayed})))))
