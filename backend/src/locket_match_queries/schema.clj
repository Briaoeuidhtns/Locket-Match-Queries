(ns locket-match-queries.schema
  (:refer-clojure :exclude [load])
  (:require
   [clojure.edn :as edn]
   [clojure.java.io :as io]
   [clojure.spec.alpha :as s]
   [clojure.spec.gen.alpha :as gen]
   [com.walmartlabs.lacinia.schema :as schema]
   [com.walmartlabs.lacinia.util :as util]
   [com.walmartlabs.lacinia.resolve :as resolve]
   [locket-match-queries.db.queries :as q]
   [slingshot.slingshot :refer [throw+]]
   [taoensso.timbre :as log]
   [locket-match-queries.scalar :as scalar]))

;; TODO make all resolvers async to take advantage of db thread pool

(defn- mock
  "Get a function that validates args against the declared spec, and returns a generated value"
  [v]
  (log/warn "Mocking" v)
  (fn [& args]
    (let [{args-spec :args ret-spec :ret} (s/get-spec v)]
      (when (some nil? '(args-spec ret-spec))
        (throw+ {:type ::missing-spec :var v}))
      (s/assert args-spec args)
      ;; initial results are pretty short and boring
      (last (gen/sample (s/gen ret-spec) 50)))))

(defn heroes [{db :locket-match-queries.server/db} _ _] (q/get-heroes db))

(defn team
  [{db :locket-match-queries.server/db} {members :of} _]
  ;; TODO populate cache first
  (-> {:matches ((mock #'q/get-matches-with) db members)
       :played_heroes ((mock #'q/get-frequent-heroes-of) db members)}
      (resolve/with-context {::team-members members})))

(defn Match->players
  [{members ::team-members db :locket-match-queries.server/db} _ {match-id :id}]
  ;; Add extra context to get items in nested resolvers
  (map #(assoc % ::match-id match-id)
    ((mock #'q/get-player-info-of-from) db members match-id)))

(defn HeroEntry->played-by
  [{members ::team-members db :locket-match-queries.server/db}
   _
   {{hero-id :id} :hero}]
  ((mock #'q/get-player-successes-with) db members hero-id))

(defn MatchPlayerEntry->items
  [{db :locket-match-queries.server/db} _ {match-id ::match-id player-id :id}]
  ((mock #'q/get-items-for) db player-id match-id))

(defn Player->display-name
  [{db :locket-match-queries.server/db} _ {player-id :id}]
  ((mock #'q/get-player-display) db player-id))

(def resolver-map
  {:query/heroes heroes
   :query/team team
   :HeroEntry/played-by HeroEntry->played-by
   :Match/players Match->players
   :MatchPlayerEntry/items MatchPlayerEntry->items
   :Player/display-name Player->display-name})

(defn load
  []
  (let [schema (with-open [r (io/reader (io/resource "schema.edn"))
                           pb (java.io.PushbackReader. r)]
                 (edn/read pb))]
    (when (:scalars schema)
      ;; So I don't forget and duplicate
      (throw+
        {:type ::scalars-defined-in-schema
         :msg
           "Scalars should be defined using `defscalar`, not in schema edn"}))
    (log/info "Attaching with scalar defs" scalar/definitions)
    (-> schema
        ;; Attach as full definitions instead of defining parsers seperately
        (assoc :scalars scalar/definitions)
        (util/attach-resolvers resolver-map)
        schema/compile)))
