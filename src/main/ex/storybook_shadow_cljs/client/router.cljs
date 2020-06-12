(ns ex.storybook-shadow-cljs.client.router
  (:require
    [cljs.spec.alpha :as s]
    [ex.storybook-shadow-cljs.client.application :refer [SPA]]
    [ex.storybook-shadow-cljs.client.prn-debug :refer [pprint-str]]
    [clojure.string :as str]
    [com.fulcrologic.fulcro.routing.dynamic-routing :as dr]
    [com.fulcrologic.fulcro.dom :as dom]
    [com.fulcrologic.guardrails.core :refer [>defn => | ?]]
    [dv.fulcro-util :as fu]
    [goog.object :as g]
    [reitit.core :as r]
    [reitit.frontend :as rf]
    [reitit.frontend.easy :as rfe]
    [taoensso.timbre :as log]))

(s/def ::route-segment (s/keys :req-un [::segment ::id]))
(s/def ::segment any?)
(comment (s/conform ::route-segment {:segment 5 :id 56}))

(s/def ::route (s/every-kv keyword? ::route-segment))
(comment (s/conform ::route {:main {:segment 5 :id :hi}}))

;; these are used for routing to fulcro components
;; todo come up with combined data design for the routing to deal with
;; xforming btwn url and app state
(def routes
  {:signup {:segment ["signup"] :id :signup}
   :debug  {:segment ["debug"] :id :debug}})

(def reitit-routes
  [["/" {:name :root}]
   ["/debug" {:name :debug}]
   ["/signup" {:name :signup}]])

;; example of possible redirect logic setup:
;   ["/goals" {:name :goals :redirect-to {:route :goals-date :params (fn [] {:date (t/today)})}}]

(def router (rf/router reitit-routes))

(defn route-segment [id] (-> routes id :segment))

(defn route-href [id]
  (let [conf (id routes)]
    (if-let [params (:params conf)]
      (rfe/href id params)
      (rfe/href id))))

(comment
  (println (:main routes))
  (s/explain ::segment {:segment "" :id 5})
  (s/explain ::segment (:main routes)))

(>defn url-path->vec
  [path]
  [string? => (s/coll-of string? :kind vector?)]
  (let [s (->> (str/split path "/")
            (remove empty?)
            vec)]
    ;; Not sure I want to do the default route here
    (if (seq s) s [])))

(defn on-match
  [SPA router m]
  ;; Looks like the first match comes in as nil when init! is called.
  (let [m (or m {:path (g/get js/location "pathname")})]
    (log/trace "match: " (pprint-str m))
    (let [{:keys [path]} m]
      (log/trace "path: " path)
      ;; route has redirect
      (if-let [{:keys [route params]} (get-in m [:data :redirect-to])]
        (let [params (params)]
          (do (log/info "redirecting to: " route " with params " params)
              (js/setTimeout #(rfe/replace-state route params))))

        (let [parts (url-path->vec path)
              route-exists? (rf/match-by-path router path)]

          ;(log/info "parts: " parts)
          ;(log/info "route-exists? : " route-exists?)
          (if route-exists?
            (do
              ;(log/info "Handling parts: " parts)
              ;; This puts the path on /main, but it also works just leaving it at /
              (if (= path "/")
                (do
                  (log/trace "push-state :goals.")
                  ;; This fails if not using setTimeout
                  (js/setTimeout #(rfe/push-state :root)))
                (do (log/info "In on-match dr/change-route to " parts)
                    (dr/change-route! SPA parts))))
            ;; unknown page, redirect to root
            (js/setTimeout #(rfe/push-state :root))))))))

(defn init! [SPA]
  (log/info "STARTING ROUTER")
  (dr/initialize! SPA)
  (rfe/start! router (partial on-match SPA router) {:use-fragment false}))

(defn current-route [this]
  (some-> (dr/current-route this this) first keyword))

(defn current-app-route []
  (dr/current-route SPA))

(defn current-route-from-url []
  (let [match (rf/match-by-path router (g/get js/location "pathname"))]
    (log/info "match: " match)
    (some-> match :data :name)))

(defn current-full-route-from-url []
  (let [match (rf/match-by-path router (g/get js/location "pathname"))]
    (log/info "match: " match)
    match))

(defn route=url?*
  [route-key params match]
  (let [{curr-params :path-params} match
        {curr-name :name} (:data curr-params)]
    (boolean
      (when-let [{:keys [id]} (routes route-key)]
        (and
          (= id curr-name)
          (= params curr-params))))))

(defn route=url?
  "predicate does the :key like :goals {:date \"2020-05-20\"}
  match current reitit match of the url"
  [route-key params]
  (route=url?* route-key params (current-full-route-from-url)))
(comment (route=url? :goals {:date "2020-05-12"}))

(>defn change-route!
  ([this route-key]
   [some? keyword? => any?]
   (let [{:keys [id segment] :as route} (get routes route-key)]
     (rfe/push-state id)
     (dr/change-route! this segment)))

  ([this route-key params]
   [some? keyword? map? => any?]
   (let [current-route (current-route-from-url)
         {:keys [id segment] :as route} (get routes route-key)
         segment (mapv (fn [p] (if (contains? params p) (get params p) p)) segment)]
     (when-not (route=url? route-key params)
       (rfe/push-state id params)
       (dr/change-route! this segment params)))))

(>defn change-route-rel!
  [this route-key]
  [some? keyword? => any?]
  (let [{:keys [id segment] :as route} (get routes route-key)]
    (rfe/push-state id)
    (dr/change-route-relative! this this segment)))

(defn link
  ([target]
   (link target {}))
  ([target opts]
   (let [current-route (current-route-from-url)]
     (dom/a :.item
       (merge
         {:classes [(when (= target current-route) "active")]
          :key     (str target)
          :href    (if (fu/on-server?)
                     (name target)
                     (route-href target))}
         opts)
       (str/capitalize (name target))))))
