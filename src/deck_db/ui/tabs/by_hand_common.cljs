(ns deck-db.ui.tabs.by-hand-common
  (:require
   [clojure.string :as str]
   [deck-db.codec :as codec]
   [deck-db.ui.common :as ui]))

(def suits ["clubs" "diamonds" "hearts" "spades"])
(def suit-symbols {"clubs" "♣" "diamonds" "♦" "hearts" "♥" "spades" "♠"})
(def values ["A" "2" "3" "4" "5" "6" "7" "8" "9" "10" "J" "Q" "K"])

(defn display-char [c]
  (if (= c \space) " " (str c)))

(defn idx->card-name [idx]
  (let [suit-idx (quot idx 13)
        val-idx  (rem idx 13)
        suit     (nth suits suit-idx)
        value    (nth values val-idx)]
    (str value (get suit-symbols suit))))

(def ^:private sup-chars [\⁰ \¹ \² \³ \⁴ \⁵ \⁶ \⁷ \⁸ \⁹])
(def ^:private sub-chars [\₀ \₁ \₂ \₃ \₄ \₅ \₆ \₇ \₈ \₉])

(defn- num->unicode [char-vec n]
  (apply str (map #(nth char-vec (- (int %) (int \0))) (str n))))

(defn sup [n] (num->unicode sup-chars n))
(defn sub [n] (num->unicode sub-chars n))

(defn charset-table []
  (let [indexed (vec (map-indexed vector codec/charset))
        total (count indexed)
        num-rows 10
        num-cols (Math/ceil (/ total num-rows))]
    [ui/blackboard
     [:table {:class "text-sm font-mono border-collapse"}
      [:tbody
       (for [ri (range num-rows)]
         ^{:key ri}
         [:tr
          (for [ci (range num-cols)
                :let [i (+ (* ci num-rows) ri)]
                :when (< i total)]
            (let [[idx c] (nth indexed i)]
              ^{:key idx}
              [:<>
               [:td {:class (str "text-[" ui/color-text-muted "] text-right pr-1 py-0.5 tabular-nums select-none")} (str idx)]
               [:td {:class (str "text-[" ui/color-text "] pl-1 pr-7 py-0.5 tabular-nums")} (display-char c)]]))])]]]))

(defn section-header [title]
  [:h2
   {:class (str "text-[" ui/color-highlight "] text-lg font-bold mt-6 mb-3 border-b border-[" ui/color-border "] pb-1")}
   title])

(defn step-header [h1 h2]
  [:h2 {:class (str "font-bold text-lg mt-6 mb-3 border-b border-[" ui/color-border "] pb-1")}
   [:span {:class (str "text-[" ui/color-highlight "]")} h1 ":"]
   " "
   [:span {:class (str "text-[" ui/color-text "]")} h2 ]])

(defn step-num [n]
  [:span {:class (str "text-[" ui/color-highlight "] font-bold")} (str "Step " n ": ")])

(defn card-index-table []
  [ui/blackboard
   [:table {:class "text-xs font-mono border-collapse"}
    [:thead
     [:tr
      [:th {:class (str "text-left text-[" ui/color-text-muted "] pb-2 pr-4 font-normal")} "Suit"]
      (for [v values]
        ^{:key v}
        [:th {:class (str "text-[" ui/color-text-muted "] pb-2 px-2 text-center font-normal")} v])]]
    [:tbody
     (for [[si s] (map-indexed vector suits)]
       ^{:key s}
       [:tr
        [:td {:class (str "text-[" ui/color-text-secondary "] pr-4 py-1 whitespace-nowrap")}
         (str (get suit-symbols s) " " (str/capitalize s))]
        (for [vi (range 13)]
          ^{:key vi}
          [:td {:class (str "text-[" ui/color-text-accent "] text-center px-2 py-1 tabular-nums")}
           (str (+ (* si 13) vi))])])]]])

(defn code-block [s]
  [:pre
   {:class (str "bg-[" ui/color-bg-surface "] rounded-lg px-4 py-3 text-xs text-[" ui/color-text-accent "] font-mono overflow-x-auto mb-4 leading-relaxed")}
   s])

(defn formula [s]
  [:div
   {:class (str "bg-[" ui/color-bg-surface "] rounded px-4 py-2 font-mono text-sm text-[" ui/color-text-accent "] mb-3 tracking-wide")}
   s])
