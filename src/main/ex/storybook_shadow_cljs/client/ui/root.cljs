(ns ex.storybook-shadow-cljs.client.ui.root
  (:require
    [com.fulcrologic.fulcro.application :as app]
    [com.fulcrologic.fulcro.components :as c :refer [defsc]]
    [com.fulcrologic.fulcro.dom :as dom :refer [div]]
    [com.fulcrologic.fulcro.routing.dynamic-routing :as dr]
    [com.fulcrologic.fulcro.ui-state-machines :as sm]
    [ex.storybook-shadow-cljs.client.ui.task-item :refer [ui-task-list TaskList TaskForm ui-task-form]]
    [ex.storybook-shadow-cljs.client.application :refer [SPA]]
    [ex.storybook-shadow-cljs.client.router :as r :refer [routes change-route!]]
))

(defsc Hello [this {:keys [task-list task-form]}]
  {:query         [{:task-list (c/get-query TaskList)}
                   {:task-form (c/get-query TaskForm)}]
   :route-segment (r/route-segment :hello)
   :initial-state (fn [_] {:task-form (c/get-initial-state TaskForm)})
   :ident         (fn [_] [:component/id :hello])}
  [:div
   [:h1 "Here's a task form:"]
   (ui-task-form task-form)
   [:h1 "Here's a task list:"]
   (ui-task-list task-list)])

(dr/defrouter TopRouter
  [this {:keys [current-state route-factory route-props]}]
  {:router-targets [Hello]})

(def ui-top-router (c/factory TopRouter))

 (defn menu [{:keys [current-tab]}]
   (div :.ui.secondary.pointing.menu
     (mapv
       #(r/link % current-tab) [:hello])))


  (defsc PageContainer [this {:root/keys [router] :as props}]
    {:query         [{:root/router (c/get-query TopRouter)}
                     [::sm/asm-id ::TopRouter]]
     :ident         (fn [] [:component/id :page-container])
     :initial-state (fn [_] {:root/router (c/get-initial-state TopRouter {})})}
    (let [current-tab (r/current-route this)]
      [:.ui.container
       ^:inline (menu {:current-tab current-tab})
       ^:inline (ui-top-router router)]))

  (def ui-page-container (c/factory PageContainer))

  (defsc Root [_ {:root/keys [page-container]}]
    {:query         [{:root/page-container (c/get-query PageContainer)}]
     :initial-state (fn [_] {:root/page-container (c/get-initial-state PageContainer {})})}
    ^:inline (ui-page-container page-container))
