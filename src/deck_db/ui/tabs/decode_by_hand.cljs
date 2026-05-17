(ns deck-db.ui.tabs.decode-by-hand
  (:require
   [reagent.core :as r]
   [deck-db.codec :as codec]
   [deck-db.ui.tabs.by-hand-common :as bhc]
   [deck-db.ui.common :as ui]
   [deck-db.ui.deck-hash :as h]))

(defonce *perm (r/atom (vec (range codec/deck-size))))
(defonce *dragging-pos (r/atom nil))
(defonce *deck-str-input (r/atom (h/perm->str (vec (range codec/deck-size)))))

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
    :on-drag-start (fn [_] (reset! *dragging-pos pos))
    :on-drag-over (fn [e] (.preventDefault e))
    :on-drop (fn [_]
               (when-let [from @*dragging-pos]
                 (swap! *perm reorder from pos)
                 (reset! *deck-str-input (h/perm->str @*perm)))
               (reset! *dragging-pos nil))}
   [ui/card-img card-idx]])

(defn- card-grid [perm-val]
  [:div
   {:class "flex flex-wrap"
    :style {:width (str (* 13 (+ ui/card-width-px ui/card-gap-px)) "px")}}
   (for [[pos card-idx] (map-indexed vector perm-val)]
     ^{:key card-idx}
     [draggable-card pos card-idx])])

(def ^:private ex-perm (codec/encode "Hello, world!"))
(def ^:private ex (codec/decode-steps ex-perm))

(defn tab []
  (let [p       @*perm
        message (codec/decode p)]
    [:div {:class (str "text-[" ui/color-text-secondary "] pb-10 font-mono")}

     [:p {:class "text-sm mb-3"}
      "Given a deck of " codec/deck-size " cards in a specific order, this process extracts the hidden message. "
      "It reverses the encoding: convert the card ordering into a integer using the "
      [:em "factoradic"] " system, then convert that integer from base " codec/charset-size " back into characters."]

     [:div {:class "space-y-4 mb-8"}
      [h/in-view {:*string *deck-str-input
                  :*deck *perm}]
      [card-grid p]]


     ;; STEP 1 ---

     [:div {:class "space-y-4"}
      [bhc/step-header "Step 1" "Number Each Card"]
      [:p {:class "text-sm"}
       "Look up every card in the reference table and write down its index (0–" (dec codec/deck-size) "). "]
      [bhc/card-index-table]

      [ui/blackboard
       [:table {:class "text-xs font-mono border-collapse"}
        [:tbody
         [:tr
          (map-indexed
           (fn [i _]
             ^{:key i}
             [:td {:class (str "text-[" ui/color-text-muted "] text-center px-2 py-1")} i])
           ex-perm)]
         [:tr
          (map-indexed
           (fn [i card-idx]
             ^{:key i}
             [:td {:class (str "text-[" ui/color-text-accent "] text-center px-2 py-1")} (bhc/idx->card-name card-idx)])
           ex-perm)]
         [:tr
          (map-indexed
           (fn [i card-idx]
             ^{:key i}
             [:td {:class (str "text-[" ui/color-text-secondary "] text-center px-2 py-1 tabular-nums")} card-idx])
           ex-perm)]]]]]

     ;; STEP 2 ---

     [:div {:class "space-y-4"}
      [bhc/step-header "Step 2" "Compute the Lehmer Code"]

      [:p {:class "text-sm mb-2"}
       "Start with a list of all " codec/deck-size " indices in order:"]
      [bhc/formula (str "available = [0, 1, 2, 3, …, " (dec codec/deck-size) "]")]
      [:p {:class "text-sm"} "For each card in your deck, reading left to right:"]
      [:ol {:class "text-sm list-none space-y-1 pl-2"}
       [:li [:span {:class (str "text-[" ui/color-highlight "]")} "a. "]
        "Find the card's index in "
        [:code {:class (str "text-[" ui/color-text-accent "] font-mono text-xs")} "available"]
        " — call its position d (0-indexed, so the first entry is position 0)"]
       [:li [:span {:class (str "text-[" ui/color-highlight "]")} "b. "]
        "Write down d as the next Lehmer digit: L₀, L₁, L₂, …"]
       [:li [:span {:class (str "text-[" ui/color-highlight "]")} "c. "]
        "Remove this card from "
        [:code {:class (str "text-[" ui/color-text-accent "] font-mono text-xs")} "available"]
        " and move to the next card"]]
      [:p {:class (str "text-sm text-[" ui/color-text-muted "]")}
       "You'll get " codec/deck-size " Lehmer digits L₀–L" (bhc/sub (dec codec/deck-size)) ". "
       "L₀ is in 0–" (dec codec/deck-size) ", L₁ is in 0–" (- codec/deck-size 2) ", …, L"
       (bhc/sub (dec codec/deck-size)) " is always 0."]

      [ui/blackboard
       [:table {:class "text-xs font-mono border-collapse"}
        [:tbody
         [:tr
          (map-indexed
           (fn [i _]
             ^{:key i}
             [:td {:class (str "text-[" ui/color-text-muted "] text-center px-2 py-1")} (str "L" (bhc/sub i))])
           (:lehmer-digits ex))]
         [:tr
          (map-indexed
           (fn [i d]
             ^{:key i}
             [:td {:class (str "text-[" ui/color-text-secondary "] text-center px-2 py-1 tabular-nums")} d])
           (:lehmer-digits ex))]]]]]

     ;; STEP 3 ---

     [:div {:class "space-y-4"}
      [bhc/step-header "Step 3" "Reconstruct the Big Integer N"]

      [:p {:class "text-sm mb-2"}
       "Combine the Lehmer digits using factorials:"]
      [bhc/formula (str "N = L₀ × " (dec codec/deck-size) "!"
                        "  +  L₁ × " (- codec/deck-size 2) "!"
                        "  +  …  +  L" (bhc/sub (dec codec/deck-size)) " × 0!")]
      [:p {:class (str "text-sm text-[" ui/color-text-muted "]")}
       "N can be up to ~8×10⁶⁷."]

      [bhc/code-block (str "N = " (:N ex))]]

     ;; STEP 4 ---

     [:div {:class "space-y-4"}
      [bhc/step-header "Step 4" (str "Convert N to " codec/max-chars " Base-" codec/charset-size " Digits")]

      [:p {:class "text-sm"}
       "Repeatedly divide N by " codec/charset-size ", collecting remainders — this gives digits least-significant first. "
       "Repeat exactly " codec/max-chars " times, then reverse the list:"]
      [:ol {:class "text-sm list-none space-y-1 pl-2"}
       [:li [:span {:class (str "text-[" ui/color-highlight "]")} "a. "]
        "digit = N mod " codec/charset-size ",  N = N ÷ " codec/charset-size]
       [:li [:span {:class (str "text-[" ui/color-highlight "]")} "b. "]
        "Repeat " codec/max-chars " times, collecting each digit"]
       [:li [:span {:class (str "text-[" ui/color-highlight "]")} "c. "]
        "Reverse the collected digits — now d₀ is most significant"]]
      [:p {:class (str "text-sm text-[" ui/color-text-muted "]")}
       "You now have " codec/max-chars " numbers d₀–d" (bhc/sub (dec codec/max-chars))
       ", each in 0–" (dec codec/charset-size) "."]

      [ui/blackboard
       [:table {:class "text-xs font-mono border-collapse"}
        [:tbody
         [:tr
          (map-indexed
           (fn [i _]
             ^{:key i}
             [:td {:class (str "text-[" ui/color-text-muted "] text-center px-2 py-1")} (str "d" (bhc/sub i))])
           (:char-indices ex))]
         [:tr
          (map-indexed
           (fn [i d]
             ^{:key i}
             [:td {:class (str "text-[" ui/color-text-secondary "] text-center px-2 py-1 tabular-nums")} d])
           (:char-indices ex))]]]]]

     ;; STEP 5 ---

     [:div {:class "space-y-4"}
      [bhc/step-header "Step 5" "Convert Each Digit to a Character"]

      [:p {:class "text-sm"}
       "Look up each digit in the charset table above to get your " codec/max-chars " characters."]

      [bhc/charset-table]

      [:p {:class (str "text-sm text-[" ui/color-text-muted "]")}
       "Trim any leading spaces — that is your decoded message."]

      [:textarea
       {:read-only true
        :rows 1
        :value message
        :class (str "w-full bg-white rounded-md "
                    "p-3 text-black font-mono text-base leading-relaxed "
                    "resize-none focus:outline-none focus:border-[" ui/color-highlight "] "
                    "placeholder-[" ui/color-input-placeholder "]")}]
      ]]))
