(ns deck-db.panels.by-hand-common
  (:require
   [clojure.string :as str]
   [deck-db.codec :as codec]))

(def suits ["clubs" "diamonds" "hearts" "spades"])
(def suit-symbols {"clubs" "♣" "diamonds" "♦" "hearts" "♥" "spades" "♠"})
(def values ["A" "2" "3" "4" "5" "6" "7" "8" "9" "10" "J" "Q" "K"])

(defn- display-char [c]
  (if (= c \space) "·" (str c)))

(def ^:private sup-chars [\⁰ \¹ \² \³ \⁴ \⁵ \⁶ \⁷ \⁸ \⁹])
(def ^:private sub-chars [\₀ \₁ \₂ \₃ \₄ \₅ \₆ \₇ \₈ \₉])

(defn- num->unicode [char-vec n]
  (apply str (map #(nth char-vec (- (int %) (int \0))) (str n))))

(defn sup [n] (num->unicode sup-chars n))
(defn sub [n] (num->unicode sub-chars n))

(defn charset-table []
  (let [cols 8
        rows (partition cols (map-indexed vector codec/charset))]
    [:table {:class "text-xs font-mono border-collapse mb-4"}
     [:tbody
      (for [[ri row] (map-indexed vector rows)]
        ^{:key ri}
        [:tr
         (for [[idx c] row]
           ^{:key idx}
           [:<>
            [:td {:class "text-[#999] text-right pr-1 py-0.5 tabular-nums select-none"} (str idx)]
            [:td {:class "text-[#7ea974] pl-1 pr-5 py-0.5 tabular-nums"} (display-char c)]])])]]))

(defn section-header [title]
  [:h2
   {:class "text-[#c8a84b] text-lg font-bold mt-6 mb-3 border-b border-[#333] pb-1"}
   title])

(defn step-num [n]
  [:span {:class "text-[#c8a84b] font-bold"} (str "Step " n ": ")])

(defn card-index-table []
  [:div {:class "overflow-x-auto mb-4"}
   [:table {:class "text-xs font-mono border-collapse"}
    [:thead
     [:tr
      [:th {:class "text-left text-[#999] pb-2 pr-4 font-normal"} "Suit"]
      (for [v values]
        ^{:key v}
        [:th {:class "text-[#999] pb-2 px-2 text-center font-normal"} v])]]
    [:tbody
     (for [[si s] (map-indexed vector suits)]
       ^{:key s}
       [:tr
        [:td {:class "text-[#ccc] pr-4 py-1 whitespace-nowrap"}
         (str (get suit-symbols s) " " (str/capitalize s))]
        (for [vi (range 13)]
          ^{:key vi}
          [:td {:class "text-[#7ea974] text-center px-2 py-1 tabular-nums"}
           (str (+ (* si 13) vi))])])]]])

(defn code-block [s]
  [:pre
   {:class "bg-[#0d0d0d] rounded-lg px-4 py-3 text-xs text-[#7ea974] font-mono overflow-x-auto mb-4 leading-relaxed"}
   s])

(defn formula [s]
  [:div
   {:class "bg-[#0d0d0d] rounded px-4 py-2 font-mono text-sm text-[#7ea974] mb-3 tracking-wide"}
   s])
