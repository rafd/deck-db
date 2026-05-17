(ns codeck.core
  (:require
   [reagent.dom :as rdom]
   [codeck.ui.app :as app]
   ))

(defn mount []
  (rdom/render [app/app]
               (.getElementById js/document "app")))

(defn on-js-reload []
  (mount))

(defn ^:export main []
  (mount))
