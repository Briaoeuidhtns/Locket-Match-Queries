(ns simple-compojure.core
  (require
    [ring.adapter.jetty :refer [run-jetty]]
    [ring.middleware.params :as p]
  ))

(def app
  (-> r/routes
      p/wrap-params))

(defonce server
  (run-jetty #'app {:port 8080 :join? false}))
