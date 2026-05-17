(ns deck-db.ui.common
  (:require
   [deck-db.cards :as cards]))

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
