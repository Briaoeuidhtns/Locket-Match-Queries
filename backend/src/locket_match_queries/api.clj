(ns locket-match-queries.api
  (:require
   [camel-snake-kebab.core :as csk]
   [clj-http.client :as http]
   [clojure.spec.alpha :as s]
   [locket-match-queries.api.spec.match :as match]
   [locket-match-queries.api.spec.player :as player]
   [clojure.core.async :refer [>!! alt! timeout go-loop chan close!] :as a]
   [taoensso.timbre :as log]
   [com.stuartsierra.component :as component]
   [clojure.string :refer [upper-case]])
  (:import
   (java.time Duration)))

(defprotocol KeyProvider
  "A provider for an api key.

  Mainly to provide optional support for using a `LimitGen` to provide access"
  (->key [self]
         "Get an api key isntance. Assumed valid for a single api call."))

(extend-protocol KeyProvider
  String
    (->key [self] self)
  Number
    (->key [self]
      (-> self
          biginteger
          (.toString 16)
          upper-case)))

(defrecord LimitGen [key window rate remaining exit-ch]
  component/Lifecycle
    (start [self]
      (let [exit-ch (chan)
            remaining (atom 0)]
        (go-loop []
                 (log/info "Resetting remaining calls to"
                           (reset! remaining rate))
                 (alt! (timeout (.toMillis window))
                       (recur)
                       exit-ch
                       (log/info "Stopping limit gen")))
        (assoc self
          :remaining remaining
          :exit-ch exit-ch)))
    (stop [self]
      (close! exit-ch)
      (assoc self
        :remaining nil
        :exit-ch nil))
  KeyProvider
    (->key [self]
      (let [rem-inst (swap! remaining dec)]
        (if (neg? rem-inst)
          (throw (ex-info "Rate limited"
                          {:type ::rate-limited :misses (- rem-inst)}))
          (->key key)))))

(defn new-default-limit-gen
  "Create a new `LimitGen` with a rate to match valve's documented api rate limits for the given key"
  [key]
  {:key (map->LimitGen {:key key :window (Duration/ofDays 1) :rate 100000})})

(defn ^:private mkurl
  [endpoint]
  (format "https://api.steampowered.com/%s" endpoint))

(s/fdef mkurl
  :args (s/cat :endpoint string?)
  :ret string?)

;; Transducers are really overkill for a single value, guessing most of them
;; will end up being trivial `map` based ones. Keeps things consistent though,
;; and shouldn't be that much extra work
(defn ^:private api-call-fn
  "Define a function to call an api endpoint, optionally with a transducer.

  Returns a channel with the optional transducer that will receive a single
  response map on success or exception on failure, and then close."
  ([endpt] (api-call-fn endpt nil))
  ([endpt xform]
   (fn [key-provider & {:as params}]
     (let [ch (a/chan 1 xform)
           handle (partial >!! ch)]
       (try (->key key-provider)
            (http/get
              (mkurl endpt)
              {:as :json :async? true :query-params (assoc params :key key)}
              ;; Errors are all inst of `Exception`,
              ;; so differentiate elsewhere
              handle
              handle)
            (catch Exception e (a/offer! ch e)))
       ch))))

(defn ^:private split-ex-handler
  [& {:keys [on-success on-failure]
      :or {on-success identity on-failure identity}}]
  (fn [val] ((if-not (instance? Throwable val) on-success on-failure) val)))

(def get-match-data
  (api-call-fn "IDOTA2Match_570/GetMatchDetails/v1"
               (map #(get-in % [:body :result]))))
(s/fdef get-match-data :args (s/keys* :req-un [::match/match_id]))

(def get-hero-data
  (letfn [(process
            [val]
            (map #(update %
                          :name
                          (comp csk/->kebab-case-keyword
                                (partial re-find #"(?<=npc_dota_hero_).*")))
              (get-in val [:body :result :heroes])))]
    (api-call-fn "IEconDOTA2_570/GetHeroes/v1"
                 (map (split-ex-handler :on-success process)))))
(s/fdef get-hero-data :args (s/keys*))

(def recent-matches
  "Get recent matches by player id, or all players if omitted"
  (api-call-fn "IDOTA2Match_570/GetMatchHistory/v1"
               (map (split-ex-handler :on-success
                                      #(get-in % [:body :result :matches])))))
(s/fdef recent-matches
  :args (s/cat :params (s/keys* :opt-un [::player/account_id])))

(def get-item-data
  (api-call-fn "IEconDOTA2_570/GetGameItems/v1"
               (map (split-ex-handler :on-success
                                      #(get-in % [:body :result :items])))))
(s/fdef get-item-data :args (s/keys*))

(defn get-unique-match-ids [matches] (into #{} (map :match_id) matches))
(s/fdef get-unique-match-ids
  :args (s/cat :matches ::match/match))
