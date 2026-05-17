(ns deck-db.panels.encode
  (:require
   [reagent.core :as r]
   [deck-db.codec :as codec]
   [deck-db.ui.deck-hash :as h]
   [deck-db.ui.common :as ui]))

(def ^:private text (r/atom ""))

(defn- char-counter []
  (let [n     (count @text)
        limit codec/max-chars
        over? (> n limit)]
    [:div
     {:class (str "text-right tabular-nums "
                  (when over? "text-red-400"))}
     [:span  n " / " limit]]))

(defn- card-grid [perm]
  [:div
   {:class "flex flex-wrap"
    :style {:gap (str ui/card-gap-px "px")
            :width (str ui/width "px")}}
   (for [[pos card-idx] (map-indexed vector perm)]
     ^{:key pos}
     [ui/card-img card-idx])])

(defn panel []
  (let [t    @text
        perm (codec/encode t)]
    [:div {:class "space-y-2"}
     [:div
      [:textarea
       {:class (str "w-full bg-white rounded-md "
                    "p-3 text-black font-mono text-base leading-relaxed "
                    "resize-none focus:outline-none focus:border-[#c8a84b] "
                    "placeholder-[#aaa]")
        :rows 1
        :placeholder "Type a message… "
        :value t
        :on-change (fn [e]
                     (->> (.. e -target -value)
                          codec/sanitize
                          (reset! text)))}]
      [:div {:class "flex text-[#7ea974] tracking-wider"}
       "Character Set: A–Z, a–z, 0–9, punctuation (ASCII)"
       [:div {:class "grow"}]
       [char-counter]]]
     [ui/arrow]
     [h/out-view perm]
     [card-grid perm]]))
