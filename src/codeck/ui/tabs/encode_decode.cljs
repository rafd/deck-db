(ns codeck.ui.tabs.encode-decode
  (:require
   [reagent.core :as r]
   [codeck.codec :as codec]
   [codeck.img-codec :as img-codec]
   [codeck.ui.common :as ui]
   [codeck.ui.deck-hash :as h]))

;; Single source of truth. Text and pixels are always derived from *perm.
(defonce *perm (r/atom (vec (range codec/deck-size))))
(defonce *text (r/atom ""))
(defonce *deck-str (r/atom (h/perm->str (vec (range codec/deck-size)))))

(defn- set-perm! [p]
  (reset! *perm p)
  (reset! *text (codec/decode p))
  (reset! *deck-str (h/perm->str p)))

(defn- set-text! [raw]
  (let [t (codec/sanitize raw)
        p (codec/encode t)]
    (reset! *text t)
    (reset! *perm p)
    (reset! *deck-str (h/perm->str p))))

(defn- set-pixels! [pixels]
  (set-perm! (img-codec/encode-img pixels)))

(defn- char-counter []
  (let [n (count @*text)
        limit codec/max-chars]
    [:div
     {:class "text-right tabular-nums"}
     [:span n " / " limit]]))

(defn- pixel-grid []
  (r/with-let [*drawing (r/atom nil)]
    (let [pixels (img-codec/decode-img @*perm)]
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
                              (set-pixels! (assoc pixels i new-val))))
           :on-mouse-enter (fn [_]
                             (when-let [v @*drawing]
                               (set-pixels! (assoc pixels i v))))
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
       :value @*text
       :on-change (fn [e] (set-text! (.. e -target -value)))}]
     [:div "A–Z, a–z, 0–9, punctuation (ASCII)"]]

    [:div {:class "flex flex-col gap-1"}
     [:div {:class (str "flex items-center justify-between text-[" ui/color-text-accent "] tracking-wider")}
      "Image (15×15)"
      [:button
       {:class (str "text-[" ui/color-text-accent "] hover:text-[" ui/color-text "] "
                    "cursor-pointer bg-transparent ml-3")
        :on-click (fn [_] (set-pixels! (vec (repeat img-codec/pixel-count 0))))}
       "[ Clear ]"]]
     [pixel-grid]]]

   [:div {:class "flex justify-center"}
    [:img {:src "down.png" :width "64px"}]
    [:img {:src "down.png" :width "64px" :class "rotate-[180deg]"}]]

   [:div
    [h/in-view {:*string *deck-str
                :*deck *perm
                :on-perm-change! (fn [p]
                                   (reset! *text (codec/decode p)))}]
    [:div {:class "overflow-x-auto"}
     [ui/rearrangeable-deck *perm
      (fn []
        (let [p @*perm]
          (reset! *deck-str (h/perm->str p))
          (reset! *text (codec/decode p))))]]]])
