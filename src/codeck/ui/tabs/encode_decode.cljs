(ns codeck.ui.tabs.encode-decode
  (:require
   [reagent.core :as r]
   [codeck.codec :as codec]
   [codeck.img-codec :as img-codec]
   [codeck.ui.common :as ui]
   [codeck.ui.state :as s]))

(defn- char-counter []
  (let [n (count @s/*text)
        limit codec/max-chars]
    [:div
     {:class "text-right tabular-nums"}
     [:span n " / " limit]]))

(defn- pixel-grid []
  (r/with-let [*drawing (r/atom nil)]
    (let [pixels @s/*pixels]
      [:div
       {:style (merge (ui/pixel-grid-base-style img-codec/img-size)
                      {:user-select "none"
                       :-webkit-user-select "none"
                       :cursor "crosshair"})
        :on-mouse-up (fn [_] (reset! *drawing nil))
        :on-mouse-leave (fn [_] (reset! *drawing nil))}
       (for [i (range img-codec/pixel-count)]
         ^{:key i}
         [:div
          {:on-mouse-down (fn [e]
                            (.preventDefault e)
                            (let [new-val (if (= (nth pixels i) 0) 1 0)]
                              (reset! *drawing new-val)
                              (s/set-pixels! (assoc pixels i new-val))))
           :on-mouse-enter (fn [_]
                             (when-let [v @*drawing]
                               (s/set-pixels! (assoc pixels i v))))
           :style {:background-color (if (= (nth pixels i) 1) "#000000" "#ffffff")}}])])))

(defn tab []
  [:div {:class "space-y-4"}
   [:div {:class "flex gap-6 items-start"}

    [:div {:class (str "flex-1 flex flex-col gap-1 text-[" ui/color-text-accent "] tracking-wider")}
     [:div {:class "flex"}
      [:div {:class "grow"} "Text"]
      [char-counter]]
     [:textarea
      {:class ui/textarea-class
       :rows 4
       :placeholder "Type a message to encode…"
       :value @s/*text
       :on-change (fn [e] (s/set-text! (.. e -target -value)))}]
     [:div "A–Z, a–z, 0–9, punctuation (ASCII)"]]

    [:div {:class "flex flex-col gap-1"}
     [:div {:class (str "flex items-center justify-between text-[" ui/color-text-accent "] tracking-wider")}
      "Image (15×15)"
      [:button
       {:class (str "text-[" ui/color-text-accent "] hover:text-[" ui/color-text "] "
                    "cursor-pointer bg-transparent ml-3")
        :on-click (fn [_] (s/set-pixels! (vec (repeat img-codec/pixel-count 0))))}
       "[ Clear ]"]]
     [pixel-grid]]]

   [:div {:class "flex justify-center"}
    [:img {:src "down.png" :width "64px"}]
    [:img {:src "down.png" :width "64px" :class "rotate-[180deg]"}]]

   [:div
    [:div {:class "overflow-x-auto"}
     [ui/rearrangeable-deck s/*perm]]]])
