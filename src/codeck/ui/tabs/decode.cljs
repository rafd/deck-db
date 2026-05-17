(ns codeck.ui.tabs.decode
  (:require
   [reagent.core :as r]
   [codeck.codec :as codec]
   [codeck.ui.common :as ui]
   [codeck.ui.deck-hash :as h]))

(defonce perm (r/atom (vec (range codec/deck-size))))
(defonce deck-str-input (r/atom (h/perm->str (vec (range codec/deck-size)))))

(defn tab []
  (let [p       @perm
        message (codec/decode p)]
    [:div {:class "space-y-4"}
     [:div {:class (str "flex text-[" ui/color-text-accent "] tracking-wider")}]

     [:div
      [h/in-view {:*string deck-str-input
                  :*deck perm}]
      [ui/rearrangeable-deck perm (fn [] (reset! deck-str-input (h/perm->str @perm)))]]

     [ui/arrow]

     [:textarea
      {:read-only true
       :rows 2
       :value (if (empty? message)
                "Rearrange the cards to decode your message."
                message)
       :class ui/textarea-class}]]))
