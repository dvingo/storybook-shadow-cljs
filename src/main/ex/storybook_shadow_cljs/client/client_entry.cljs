(ns ex.storybook-shadow-cljs.client.client-entry
  (:require
    [com.fulcrologic.fulcro.application :as app]
    [com.fulcrologic.fulcro.ui-state-machines :as uism]
    [clojure.edn :as edn]
    [ex.storybook-shadow-cljs.client.ui.root :as root]
    [ex.storybook-shadow-cljs.client.application :refer [SPA]]
    [ex.storybook-shadow-cljs.client.router :as router]
    [shadow.resource :as rc]
    [taoensso.timbre :as log]))

;; set logging lvl using goog-define, see shadow-cljs.edn
(goog-define LOG_LEVEL "warn")

(def fe-config (edn/read-string (rc/inline "/config/fe-config.edn")))
(log/info "fe-config: " fe-config)

;; you can selectively edit this blacklist, or toggle "Verbose" mode in the chrome dev tools logging
;; to optionally prevent all trace level logs.
(log/info "log level is: " LOG_LEVEL)
(def log-config
  {:level        (keyword LOG_LEVEL), :ns-whitelist [],
   :ns-blacklist ["com.fulcrologic.fulcro.algorithms.tx-processing"
                  "com.fulcrologic.fulcro.algorithms.indexing"
                  "com.fulcrologic.fulcro.ui-state-machines"
                  "com.fulcrologic.fulcro.routing.dynamic-routing"
                  "com.fulcrologic.fulcro.inspect.inspect-ws"
                  "shadow.cljs.devtools.server.worker.impl"]})

;; todo load non-git-tracked vars for config here.
;; could use shadow-inline
(defn ^:export refresh []
  (log/info "Hot code Remount")
  (log/merge-config! log-config)
  (app/mount! SPA root/Root "app"))

(defn ^:export init []
  (log/merge-config! log-config)
  (log/info "Application starting.")
  (app/set-root! SPA root/Root {:initialize-state? true})
  (router/init! SPA)
   (log/info "MOUNTING APP")
  (js/setTimeout #(app/mount! SPA root/Root "app" {:initialize-state? true})))
