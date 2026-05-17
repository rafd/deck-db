(ns deck-db.ui.common
  (:require
   [deck-db.cards :as cards]))

;; Color palette
(def color-bg-app "#176e03")
(def color-bg-surface "#0d0d0d")
(def color-bg-dark "#0d3d03")
(def color-border "#333")
(def color-text "#fff")
(def color-text-secondary "#fff")
(def color-text-muted "#999")
(def color-text-accent "#7ea974")
(def color-highlight "#c8a84b")
(def color-input-placeholder "#aaa")

(def card-width-px 42)
(def card-height-px 60)
(def card-gap-px 8)
(def width (+ (* 13 card-width-px)
              (* 12 card-gap-px)))

(defn card-img [card-idx]
  (let [card (nth cards/all-cards card-idx)
        path (cards/image-path card)]
    [:img
     {:src path
      :draggable false
      :title (str (:value card) " of " (:suit card))
      :class "select-none"
      :style {:width (str card-width-px "px")
              :height (str card-height-px "px")
              :object-fit "none"
              :object-position "center"}}]))

(defn blackboard [& children]
  (into [:div {:class (str "bg-[" color-bg-surface "] rounded-lg px-4 py-3 overflow-x-auto mb-4")}]
        children))

(defn arrow []
  [:img {:src "down.png" :width "128px" :class "mx-auto"}])
