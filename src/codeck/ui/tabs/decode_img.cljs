(ns codeck.ui.tabs.decode-img
  (:require
   [reagent.core :as r]
   [codeck.img-codec :as img-codec]
   [codeck.ui.common :as ui]
   [codeck.ui.deck-hash :as h]))

(defonce perm (r/atom (vec (range img-codec/deck-size))))
(defonce deck-str-input (r/atom (h/perm->str (vec (range img-codec/deck-size)))))

(defn- pixel-display [pixels]
  [:div
   {:style (ui/pixel-grid-base-style img-codec/img-size)}
   (for [i (range img-codec/pixel-count)]
     ^{:key i}
     [:div
      {:style {:background-color (if (= (nth pixels i) 1) "#000000" "#ffffff")}}])])

(defn tab []
  (let [p @perm
        pixels (img-codec/decode-img p)]
    [:div {:class "space-y-4"}
     [h/in-view {:*string deck-str-input
                 :*deck perm}]
     [ui/rearrangeable-deck perm (fn [] (reset! deck-str-input (h/perm->str @perm)))]
     [ui/arrow]
     [:div {:class "flex justify-center"}
      [pixel-display pixels]]]))
