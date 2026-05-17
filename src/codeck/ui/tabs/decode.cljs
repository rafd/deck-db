(ns codeck.ui.tabs.decode
  (:require
   [reagent.core :as r]
   [codeck.codec :as codec]
   [codeck.ui.deck-hash :as h]
   [codeck.ui.common :as ui]))

(defonce perm (r/atom (vec (range codec/deck-size))))
(defonce dragging-pos (r/atom nil))
(defonce deck-str-input (r/atom (h/perm->str (vec (range codec/deck-size)))))

(defn- reorder [v from to]
  (if (= from to)
    v
    (let [item    (nth v from)
          without (vec (concat (subvec v 0 from) (subvec v (inc from))))
          to'     (if (> to from) (dec to) to)]
      (vec (concat (subvec without 0 to') [item] (subvec without to'))))))

(defn- draggable-card [pos card-idx]
  [:div
   {:draggable true
    :class "cursor-grab"
    :style {:padding (str (/ ui/card-gap-px 2) "px")}
    :on-drag-start (fn [_] (reset! dragging-pos pos))
    :on-drag-over (fn [e] (.preventDefault e))
    :on-drop (fn [_]
               (when-let [from @dragging-pos]
                 (swap! perm reorder from pos)
                 (reset! deck-str-input (h/perm->str @perm)))
               (reset! dragging-pos nil))}
   [ui/card-img card-idx]])

(defn- card-grid [perm-val]
  [:div
   {:class "flex flex-wrap"
    :style {:width (str (* 13 (+ ui/card-width-px ui/card-gap-px)) "px")}}
   (for [[pos card-idx] (map-indexed vector perm-val)]
     ^{:key card-idx}
     [draggable-card pos card-idx])])

(defn tab []
  (let [p       @perm
        message (codec/decode p)]
    [:div {:class "space-y-4"}
     [:div {:class (str "flex text-[" ui/color-text-accent "] tracking-wider")}]

     [:div
      [h/in-view {:*string deck-str-input
                  :*deck perm}]
      [:div {:class "overflow-x-auto"}
       [card-grid p]]]

     [ui/arrow]

     [:textarea
      {:read-only true
       :rows 2
       :value (if (empty? message)
                "Rearrange the cards to decode your message."
                message)
       :class (str "w-full bg-white rounded-md "
                   "p-3 text-black font-mono text-base leading-relaxed "
                   "resize-none focus:outline-none focus:border-[" ui/color-highlight "] "
                   "placeholder-[" ui/color-input-placeholder "]")}]]))
