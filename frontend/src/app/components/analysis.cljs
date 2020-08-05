(ns app.components.analysis
  (:require
   [app.helix :refer [defnc]]
   [app.blueprint :as bp]
   [helix.core :refer [$]]
   [cljs-bean.core :refer [->js]]
   ["@blueprintjs/core" :refer [Label Position ProgressBar Tooltip Tree]]
   ["emotion" :refer [css]]
   [app.components.analysis.player :refer [Player]]))

(defnc
  Analysis
  [{:keys [played-heroes]}]
  (let [winrate-bar (css #js {:height "4px"})
        data (map (fn [{:keys [hero played_by]}]
                    (print (:id hero))
                    {:label (:display hero)
                     :id (:id hero)
                     :isExpanded true
                     :childNodes (map (fn [{:keys [player wins total winrate]}]
                                        {:id (:id player)
                                         :label ($ ^:native Tooltip
                                                   {:content (str wins \/ total)
                                                    ;; HACK fix spacing and just
                                                    ;; use auto
                                                    :position (bp/position
                                                                :top-left)}
                                                   ($ ^:native Label
                                                      ($ Player {:& player})
                                                      ($ ^:native ProgressBar
                                                         {:stripes false
                                                          :intent "success"
                                                          :value winrate
                                                          :class
                                                            winrate-bar})))})
                                   played_by)})
               played-heroes)]
    ($ ^:native Tree {:contents (->js data)})))
