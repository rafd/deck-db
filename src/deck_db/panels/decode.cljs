(ns deck-db.panels.decode
  (:require
   [reagent.core :as r]
   [deck-db.codec :as codec]
   [deck-db.ui.common :as ui]))

(def ^:private perm (r/atom (vec (range codec/deck-size))))
(def ^:private dragging-pos (r/atom nil))

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
    :on-drag-start (fn [_] (reset! dragging-pos pos))
    :on-drag-over (fn [e] (.preventDefault e))
    :on-drop (fn [_]
               (when-let [from @dragging-pos]
                 (swap! perm reorder from pos))
               (reset! dragging-pos nil))}
   [ui/card-img card-idx]])

(defn- card-grid [perm-val]
  [:div
   {:class "flex flex-wrap"
    :style {:gap (str ui/card-gap-px "px")
            :width (str ui/width "px")}}
   (for [[pos card-idx] (map-indexed vector perm-val)]
     ^{:key card-idx}
     [draggable-card pos card-idx])])

(defn panel []
  (let [p       @perm
        message (codec/decode p)]
    [:div {:class "space-y-4"}
     [card-grid p]
     [:div {:class "text-[#fff] font-mono text-lg p-3 bg-[#0d3d03] rounded-md min-h-[2.5rem]"}
      (if (empty? message)
        [:span {:class "text-[#999] italic"} "Rearrange the cards to decode a message"]
        message)]
     [:button
      {:class "text-[#7ea974] text-sm cursor-pointer bg-transparent border-0 p-0"
       :on-click (fn [_] (reset! perm (vec (range codec/deck-size))))}
      "Reset"]]))
