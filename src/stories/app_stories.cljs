(ns app-stories
  (:require
    ["@storybook/react" :refer (storiesOf)]
    ["@storybook/addon-actions" :refer (action)]
    ["react" :as react]
    [goog.object :as gobj]
    [sablono.core :as html :refer-macros [html]]
    [matterandvoid.story-util :refer [make-story]]
    [dv.cljs-emotion :refer (defstyled)])

;; if storybook runs of out memory, you can start it like so:
; add to package.json
;"storybook": "NODE_OPTIONS='--max-old-space-size=8192' start-storybook -p 6006",

(defstyled big-button :button
  (fn [{:keys [color]}]
    {:font-size  "2em"
     :background (or color "palevioletred")}))

(->
  (storiesOf "Button CLJS" js/module)
  (.add "Normal"

    #(html [:button {:on-Click (action "click")} "Hello World"]))
  (.add "Big" #(big-button {:onClick (action "big click")} "Hello World")))
