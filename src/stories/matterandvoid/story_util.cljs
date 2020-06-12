(ns matterandvoid.story-util
  (:require
    ["@storybook/react" :refer (storiesOf)]
    ["@storybook/addon-actions" :refer (action)]
    ["react" :as react]
    [com.fulcrologic.fulcro.algorithms.form-state :as fs]
    [com.fulcrologic.fulcro.application :as app]
    [com.fulcrologic.fulcro.components :as c]
    [com.fulcrologic.fulcro.dom :as dom]
    [com.fulcrologic.fulcro.mutations :as fm :refer [defmutation]]
    [goog.object :as gobj]
    [reagent.core :as r]
    [sablono.core :as html :refer-macros [html]]
    [dv.tick-util :as tu]
    [space.matterandvoid.client.prn-debug :refer [pprint-str]]
    [dv.cljs-emotion :refer (defstyled)]
    [space.matterandvoid.client.ui.date-picker :as picker]
    [dv.fulcro-util :as cu]
    [space.matterandvoid.goals.ui.habit-fields :as hf]
    [space.matterandvoid.goals.ui.habit-form :refer [HabitForm ui-habit-form]]
    [space.matterandvoid.goals.ui.habit-list :refer [ui-habit-debug-item HabitDebugItem HabitItem HabitList ui-habit-item ui-habit-list]]
    [space.matterandvoid.goals.ui.habit-record :refer [TaskRecord HabitRecord toggle-task-record-state ui-task-record]]
    [space.matterandvoid.goals.data.data-model :as dm]
    [taoensso.timbre :as log]
    [tick.alpha.api :as t])
  )
;;
;; todo move this to my-utils, make a macro - insert into dom fqn of the component.


(defn get-initial-state [comp params]
  (if (c/has-initial-app-state? comp)
    (c/get-initial-state comp params)
    params))

(defn make-root [Root]
  (let [generated-name (gensym)
        component-key  (keyword "storybook-fulcro" (name generated-name))]
    (c/configure-component! (fn *dyn-root* [])
      component-key
      {:initial-state (fn [_ params]
                        {:ui/root (or (get-initial-state Root params) {})})
       :query         (fn [_] [:fulcro.inspect.core/app-id {:ui/root (c/get-query Root)}])
       :render        (fn [this]
                        (let [{:ui/keys [root]} (c/props this)
                              Root     (-> Root c/class->registry-key c/registry-key->class)
                              factory  (c/factory Root)
                              computed (c/shared this ::computed)]
                          (log/info "The root data is : " (pprint-str root))
                          (if (seq root)
                            (factory
                              (cond-> root computed (c/computed computed))))))})))

(defn make-story [cls]
  (let [Root (make-root cls)
        new-cls (r/create-class
          {:component-did-mount
           (fn [this]
             (let [app (app/fulcro-app {:render-middleware (fn [this render] (html (render)))})]
               (when-let [dom-node (gobj/get this "el")]
                 (log/info "Mounting fulcro story.")
                 (app/mount! app Root dom-node {:initialize-state? true}))))
           :render
           (fn [this] (dom/div {:ref (fn [r] (gobj/set this "el" r))}))})]
    #(react/createElement new-cls)))
