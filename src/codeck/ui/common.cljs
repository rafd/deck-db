(ns codeck.ui.common
  (:require
   [reagent.core :as r]
   [codeck.cards :as cards]))

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

(def img-cell-px 24)
(def img-gap-px 0)

(def textarea-class
  (str "w-full bg-white rounded-md "
       "p-3 text-black font-mono text-base leading-relaxed "
       "resize-none focus:outline-none focus:border-[" color-highlight "] "
       "placeholder-[" color-input-placeholder "]"))

(defn pixel-grid-base-style [img-size]
  {:display "inline-grid"
   :grid-template-columns (str "repeat(" img-size ", " img-cell-px "px)")
   :grid-template-rows (str "repeat(" img-size ", " img-cell-px "px)")
   :gap (str img-gap-px "px")})

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

(defn- reorder [v from to]
  (if (= from to)
    v
    (let [item    (nth v from)
          without (vec (concat (subvec v 0 from) (subvec v (inc from))))
          to'     (if (> to from) (dec to) to)]
      (vec (concat (subvec without 0 to') [item] (subvec without to'))))))

(defn- find-pos-at [x y]
  (when-let [el (.elementFromPoint js/document x y)]
    (when-let [target (.closest el "[data-pos]")]
      (js/parseInt (.getAttribute target "data-pos")))))

(defn rearrangeable-deck
  "A drag-and-drop (mouse + touch) card grid.
   *perm     — atom holding the current permutation vector
   on-drop!  — zero-arg callback called after each successful reorder"
  [*perm on-drop!]
  (let [*dragging-pos (r/atom nil)
        do-drop! (fn [from to]
                   (when (and from to (not= from to))
                     (swap! *perm reorder from to)
                     (on-drop!)))]
    (fn [*perm _]
      [:div {:class "flex flex-wrap"}
       (for [[pos card-idx] (map-indexed vector @*perm)]
         ^{:key card-idx}
         [:div
          {:draggable true
           :data-pos pos
           :class "cursor-grab"
           :style {:padding (str (/ card-gap-px 2) "px")
                   :touch-action "none"}
           :on-drag-start (fn [_] (reset! *dragging-pos pos))
           :on-drag-over (fn [e] (.preventDefault e))
           :on-drop (fn [_]
                      (do-drop! @*dragging-pos pos)
                      (reset! *dragging-pos nil))
           :on-touch-start (fn [_] (reset! *dragging-pos pos))
           :on-touch-end (fn [e]
                           (let [touch (aget (.. e -changedTouches) 0)
                                 to (find-pos-at (.. touch -clientX) (.. touch -clientY))]
                             (do-drop! @*dragging-pos to)
                             (reset! *dragging-pos nil)))}
          [card-img card-idx]])])))

(defn card-grid [perm]
  [:div
   {:class "flex flex-wrap"
    :style {:gap (str card-gap-px "px")
            :width (str width "px")}}
   (for [[pos card-idx] (map-indexed vector perm)]
     ^{:key pos}
     [card-img card-idx])])

(defn blackboard [& children]
  (into [:div {:class (str "bg-[" color-bg-surface "] rounded-lg px-4 py-3 overflow-x-auto mb-4")}]
        children))

(defn arrow []
  [:img {:src "down.png" :width "64px" :class "mx-auto"}])
