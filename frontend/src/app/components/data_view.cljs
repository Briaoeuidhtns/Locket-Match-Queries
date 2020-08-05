(ns app.components.data-view
  (:require
   [helix.core :refer [$ <>]]
   [helix.hooks :as hook :include-macros true]
   [app.helix :refer [defnc]]
   [cljs-bean.core :refer [bean ->js]]
   ["@blueprintjs/core"
    :refer
    [Button Card Collapse H2 NonIdealState ProgressBar UL]]
   [app.components.match :refer [Match]]
   [app.components.analysis :refer [Analysis]]
   ["@apollo/client" :refer [useQuery gql]]
   [shadow.resource :as rc]))

(defnc DataCard
       [{:keys [children header loading?]}]
       (let [[open? set-open!] (hook/use-state true)]
         ($ ^:native Card
            ($ ^:native H2
               header
               ($ ^:native Button
                  {:minimal true
                   :icon (if open? "chevron-down" "chevron-right")
                   :on-click #(set-open! not)}))
            ($ ^:native Collapse
               {:is-open open?}
               (if loading? ($ ^:native ProgressBar) children)))))

(defnc DataView
       [{:keys [members]}]
       (let [{:keys [loading error data refetch]}
             (-> "app/queries/data_view.graphql"
                 rc/inline
                 gql
                 (useQuery #js {:variables #js {:team (->js members)}})
                 (bean :recursive true))]
         (when (seq members)
           (if error
             ($ ^:native NonIdealState
                {:icon "error"
                 :title "Network error"
                 :description (.-message error)
                 :action ($ ^:native Button
                            {:icon "refresh" :on-click #(refetch)}
                            "Try again")})
             (let [{{:keys [matches played_heroes]} :team} data
                   PlayerCard ($ DataCard
                                 {:header "Players"}
                                 ($ ^:native UL
                                    (for [id members]
                                      ($ :li {:key id :value id} id))))
                   MatchCard ($ DataCard
                                {:header "Matches" :loading? loading}
                                (for [{:keys [id] :as match} matches]
                                  ($ Match {:key id :match match})))
                   AnalysisCard ($ DataCard
                                   {:header "Analysis" :loading? loading}
                                   ($ Analysis {:played-heroes played_heroes}))]
               (<> PlayerCard MatchCard AnalysisCard))))))
