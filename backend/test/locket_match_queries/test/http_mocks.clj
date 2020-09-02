(ns locket-match-queries.test.http-mocks
  (:require
   [locket-match-queries.config :refer [config]]
   [slingshot.slingshot :refer [try+ throw+]]
   [taoensso.timbre :as log]
   [clj-http.client :as http]
   [clojure.string :as str]
   [clojure.edn :as edn]
   [clojure.java.io :as io]
   [camel-snake-kebab.core :as csk]
   [zprint.core :as zp]))

(def ^:private ns-static *ns*)

(defn routes-cache-file
  "Recommended file path to store the cache file.
  Pass a ns to get a new ns scoped cache. Otherwise the ns of this function is used"
  ([] (routes-cache-file ns-static))
  ([ns-name]
   (-> ns-name
       str
       csk/->snake_case
       (str/replace "." "__")
       (->> (format "test/__snapshots__/%s_routes.edn")))))

(def ^:dynamic ^:private *routes-cache*
  "Atom of a serializable map of request => response"
  nil)

(defn request-keyfn
  "Transform to use to match requests"
  ^:private [request]
  (-> request
      ;; I don't think there's a difference, but they're both accepted
      (dissoc :async :async?)
      (update :query-params dissoc :key)
      ;; prob not going to use query-string anywhere, but just in case
      (update :query-string
              (fnil str/replace "")
              ;; https://stackoverflow.com/a/1842787/2384326
              #"&key(=[^&]*)?|^key(=[^&]*)?&?"
              "")))

(def ^:private fetch-env-var
  "The environment variable that controls whether requests should be made when it isn't in the mock cache"
  "LOCKET_FETCH_TEST_MOCKS")

(def fetch-on-miss? (System/getenv fetch-env-var))

(defn intercept
  "Mock for `clj-http.client/request`

  The first argument should be the original function. Rest are identical to `clj-http.client/request` async form

  TODO:
    * Key is hidden, so can't test failure with bad keys
    * No cleanup for unused requests
    * http/request non async form not supported
    * Exceptions from this code may be given to caller raise fn
    * No clean way to switch `fetch-on-miss?` from test runner ala jest snapshots
    * slower than it should be, only write after maybe?
  "
  ([orig-fn request respond raise]
   (try+
     (let [key-req (request-keyfn request)
           resp? (@*routes-cache* key-req)]
       (respond
         (or
           resp?
           (if fetch-on-miss?
             (let [now-req
                   (-> key-req
                       (update :query-params merge (select-keys config [:key])))

                   _ (log/info "now-req" now-req)
                   new-resp (select-keys (orig-fn now-req)
                                         [:body :status :headers])]
               (swap! *routes-cache* assoc key-req new-resp)
               new-resp)
             (throw+
               {:type ::no-fake-route-found
                :message
                  (format
                    "Could not find a fake route to match the request. Set the environment variable `%s` to truthy (not %s) to fetch new mocks."
                    fetch-env-var
                    fetch-on-miss?)
                :request key-req})))))
     (catch [:type ::no-fake-route-found] _ (throw+))
     (catch Object e (raise e)))))

(defn routes-fixture
  [cache-file]
  (fn [f]
    (with-redefs [http/request (partial intercept http/request)]
      (let [initial-routes
            (or (some-> cache-file
                        io/file
                        (as-> $ (if (.exists $)
                                  $
                                  (log/warn "Routes cache file not found:" $)))
                        io/reader
                        java.io.PushbackReader.
                        edn/read)
                {})]
        (binding [*routes-cache* (atom initial-routes)]
          (f)
          (io/make-parents cache-file)
          (when (not= @*routes-cache* initial-routes)
            (with-open [ofile (io/writer cache-file)]
              (binding [*out* ofile] (zp/zprint @*routes-cache*)))))))))
