(ns app.core
  (:require
   [app.protocols]
   [app.helix :refer [defnc]]
   [helix.core :refer [$]]
   [helix.hooks :as hook :include-macros true]
   [cljs-bean.core :refer [bean]]
   ["react-dom" :as rdom]
   ["@apollo/client" :refer [ApolloClient InMemoryCache ApolloProvider]]
   ["use-error-boundary" :default use-error-boundary]
   ["@blueprintjs/core" :refer [Button FocusStyleManager NonIdealState Pre]]
   ["emotion" :refer [css]]
   ["react-router-dom" :rename {BrowserRouter Router}]
   [app.components.container :refer [Container]]
   [app.components.data-owner :refer [DataOwner]]))

(defnc ErrorHandler
       [{:keys [children]}]
       (let [{:keys [ErrorBoundary error]
              did-catch? :didCatch
              {component-stack :componentStack :as error-info} :errorInfo}
             (bean (use-error-boundary) :recursive true)]
         (hook/use-effect :auto-deps
                          (if did-catch?
                            (print {:error error :error-info error-info})))
         (if-not did-catch?
           ($ ^:native ErrorBoundary children)
           ($ ^:native NonIdealState
              {:icon "error"
               :title "Error"
               :action ($ ^:native Button
                          {:on-click (fn [] (js/location.reload) false)
                           :icon "refresh"}
                          "Reload")
               ;; Just randomly guessing at possible names...
               :description ((some-fn :message
                                      :msg
                                      :description
                                      #(.-message %)
                                      #(.-description %)
                                      pr-str)
                              error)}
              ($ ^:native Pre
                 {:class (css #js {:overflow "auto" :maxWidth "90%"})}
                 component-stack)))))

(defn ^:export main!
  "Run application startup logic."
  []
  (enable-console-print!)
  (.onlyShowFocusOnTabs FocusStyleManager)
  (rdom/render
    ($ Container
       {:dark? true}
       ($ ErrorHandler
          ($ ^:native ApolloProvider
             {:client (ApolloClient. #js
                                      {:uri "http://localhost:8888/api"
                                       :cache (InMemoryCache.)})}
             ($ Router ($ DataOwner)))))
    (js/document.getElementById "app")))
