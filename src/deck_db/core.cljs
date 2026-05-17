(ns deck-db.core
  (:require
   [reagent.dom :as rdom]
   [deck-db.ui.app :as app]
   ))

(defn mount []
  (rdom/render [app/app]
               (.getElementById js/document "app")))

(defn on-js-reload []
  (mount))

(defn ^:export main []
  (mount))
