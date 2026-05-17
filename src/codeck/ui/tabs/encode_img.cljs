(ns codeck.ui.tabs.encode-img
  (:require
   [reagent.core :as r]
   [codeck.img-codec :as img-codec]
   [codeck.ui.common :as ui]
   [codeck.ui.deck-hash :as h]))

(defonce pixels (r/atom (vec (repeat img-codec/pixel-count 0))))

(def ^:private cell-px 24)
(def ^:private gap-px 0)

(defn- pixel-grid []
  (r/with-let
   [*drawing (r/atom nil)]
   (let [ps @pixels]
     [:div
      {:style {:display "inline-grid"
               :grid-template-columns (str "repeat(" img-codec/img-size ", " cell-px "px)")
               :grid-template-rows (str "repeat(" img-codec/img-size ", " cell-px "px)")
               :gap (str gap-px "px")
               :user-select "none"
               :-webkit-user-select "none"
               :cursor "crosshair"}
       :on-mouse-up (fn [_] (reset! *drawing nil))
       :on-mouse-leave (fn [_] (reset! *drawing nil))}
      (for [i (range img-codec/pixel-count)]
        ^{:key i}
        [:div
         {:on-mouse-down (fn [e]
                           (.preventDefault e)
                           (let [new-val (if (= (nth ps i) 0) 1 0)]
                             (reset! *drawing new-val)
                             (swap! pixels assoc i new-val)))
          :on-mouse-enter (fn [_]
                            (when-let [v @*drawing]
                              (swap! pixels assoc i v)))
          :style {:background-color (if (= (nth ps i) 1) "#000000" "#ffffff")}}])])))

(defn tab []
  (let [ps @pixels
        perm (img-codec/encode-img ps)]
    [:div {:class "space-y-4"}

     [:div {:class "flex justify-center"}
      [:div {:class "inline-flex flex-col gap-2"}
       [:div {:class (str "flex items-center justify-between text-[" ui/color-text-accent "] tracking-wider")}
        "Draw a 15×15 image"
        [:button
         {:class (str "px-3 py-1 rounded text-sm border border-[" ui/color-text-muted "] "
                      "text-[" ui/color-text-muted "] hover:text-[" ui/color-text "] "
                      "cursor-pointer bg-transparent")
          :on-click (fn [_] (reset! pixels (vec (repeat img-codec/pixel-count 0))))}
         "Clear"]]
       [pixel-grid]]]
     [ui/arrow]
     [h/out-view perm]
     [:div {:class "overflow-x-auto"}
      [:div
       {:class "flex flex-wrap"
        :style {:gap (str ui/card-gap-px "px")
                :width (str ui/width "px")}}
       (for [[pos card-idx] (map-indexed vector perm)]
         ^{:key pos}
         [ui/card-img card-idx])]]]))
