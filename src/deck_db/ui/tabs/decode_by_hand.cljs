(ns deck-db.tabs.decode-by-hand
  (:require
   [deck-db.codec :as codec]
   [deck-db.tabs.by-hand-common :as bhc]))

(def ^:private ex-perm (codec/encode "Hello, world!"))
(def ^:private ex (codec/decode-steps ex-perm))

(defn tab []
  [:div {:class "text-[#ccc] pb-10 font-mono"}

   [bhc/section-header "How to Decode a Deck by Hand"]

   [:p {:class "text-sm mb-3"}
    "Given a deck of " codec/deck-size " cards in a specific order, this process extracts the hidden message. "
    "It reverses the encoding: convert the card ordering into a big integer using the "
    [:em "factoradic"] " system, then convert that integer from base " codec/charset-size " back into characters."]

   [bhc/section-header "Reference: Card → Index"]
   [bhc/card-index-table]

   [bhc/section-header "Reference: Index → Character"]
   [:p {:class "text-[#999] text-xs mb-2"}
    codec/charset-size " characters total (indices 0–" (dec codec/charset-size) ")"]
   [bhc/charset-table]

   [bhc/section-header "Steps"]

   [:div {:class "space-y-5"}
    [:div
     [:p {:class "text-sm mb-1"}
      [bhc/step-num 1] "Number each card in your deck"]
     [:p {:class "text-sm mb-1"}
      "Look up every card in the reference table above and write down its index (0–" (dec codec/deck-size) "). "
      "You should end up with a sequence of " codec/deck-size " distinct numbers."]
     [:p {:class "text-sm text-[#999]"}
      "Example start: A♣=0, K♠=" (dec codec/deck-size) ", 7♥=32, …"]]

    [:div
     [:p {:class "text-sm mb-1"}
      [bhc/step-num 2] "Compute the Lehmer code (factoradic digits)"]
     [:p {:class "text-sm mb-2"}
      "Start with a list of all " codec/deck-size " indices in order:"]
     [bhc/formula (str "available = [0, 1, 2, 3, …, " (dec codec/deck-size) "]")]
     [:p {:class "text-sm mb-1"} "For each card in your deck, reading left to right:"]
     [:ol {:class "text-sm list-none space-y-1 pl-2 mb-2"}
      [:li [:span {:class "text-[#c8a84b]"} "a. "]
       "Find the card's index in "
       [:code {:class "text-[#7ea974] font-mono text-xs"} "available"]
       " — call its position d (0-indexed, so the first entry is position 0)"]
      [:li [:span {:class "text-[#c8a84b]"} "b. "]
       "Write down d as the next Lehmer digit: L₀, L₁, L₂, …"]
      [:li [:span {:class "text-[#c8a84b]"} "c. "]
       "Remove this card from "
       [:code {:class "text-[#7ea974] font-mono text-xs"} "available"]
       " and move to the next card"]]
     [:p {:class "text-sm text-[#999]"}
      "You'll get " codec/deck-size " Lehmer digits L₀–L" (bhc/sub (dec codec/deck-size)) ". "
      "L₀ is in 0–" (dec codec/deck-size) ", L₁ is in 0–" (- codec/deck-size 2) ", …, L"
      (bhc/sub (dec codec/deck-size)) " is always 0."]]

    [:div
     [:p {:class "text-sm mb-1"}
      [bhc/step-num 3] "Reconstruct the big integer N"]
     [:p {:class "text-sm mb-2"}
      "Combine the Lehmer digits using factorials:"]
     [bhc/formula (str "N = L₀ × " (dec codec/deck-size) "!"
                       "  +  L₁ × " (- codec/deck-size 2) "!"
                       "  +  …  +  L" (bhc/sub (dec codec/deck-size)) " × 0!")]
     [:p {:class "text-sm text-[#999]"}
      "N can be up to ~8×10⁶⁷."]]

    [:div
     [:p {:class "text-sm mb-1"}
      [bhc/step-num 4] "Convert N to " codec/max-chars " base-" codec/charset-size " digits"]
     [:p {:class "text-sm mb-2"}
      "Repeatedly divide N by " codec/charset-size ", collecting remainders — this gives digits least-significant first. "
      "Repeat exactly " codec/max-chars " times, then reverse the list:"]
     [:ol {:class "text-sm list-none space-y-1 pl-2 mb-2"}
      [:li [:span {:class "text-[#c8a84b]"} "a. "]
       "digit = N mod " codec/charset-size ",  N = N ÷ " codec/charset-size]
      [:li [:span {:class "text-[#c8a84b]"} "b. "]
       "Repeat " codec/max-chars " times, collecting each digit"]
      [:li [:span {:class "text-[#c8a84b]"} "c. "]
       "Reverse the collected digits — now d₀ is most significant"]]
     [:p {:class "text-sm text-[#999]"}
      "You now have " codec/max-chars " numbers d₀–d" (bhc/sub (dec codec/max-chars))
      ", each in 0–" (dec codec/charset-size) "."]]

    [:div
     [:p {:class "text-sm mb-1"}
      [bhc/step-num 5] "Convert each digit to a character"]
     [:p {:class "text-sm mb-1"}
      "Look up each digit in the charset table above to get your " codec/max-chars " characters."]
     [:p {:class "text-sm text-[#999]"}
      "Trim any trailing spaces — that is your decoded message."]]]

   [bhc/section-header "Example: decoding \"Hello, world!\""]

   [:div {:class "space-y-5"}
    [:div
     [:p {:class "text-sm mb-1"}
      [bhc/step-num 1] "Card indices in your deck:"]
     [:div {:class "bg-[#0d0d0d] rounded-lg px-4 py-3 overflow-x-auto mb-4"}
      [:table {:class "text-xs font-mono border-collapse"}
       [:tbody
        [:tr
         (map-indexed
          (fn [i _]
            ^{:key i}
            [:td {:class "text-[#999] text-center px-2 py-1"} i])
          ex-perm)]
        [:tr
         (map-indexed
          (fn [i card-idx]
            ^{:key i}
            [:td {:class "text-[#7ea974] text-center px-2 py-1"} (bhc/idx->card-name card-idx)])
          ex-perm)]
        [:tr
         (map-indexed
          (fn [i card-idx]
            ^{:key i}
            [:td {:class "text-[#ccc] text-center px-2 py-1 tabular-nums"} card-idx])
          ex-perm)]]]]]

    [:div
     [:p {:class "text-sm mb-1"}
      [bhc/step-num 2] "Lehmer code:"]
     [:div {:class "bg-[#0d0d0d] rounded-lg px-4 py-3 overflow-x-auto mb-4"}
      [:table {:class "text-xs font-mono border-collapse"}
       [:tbody
        [:tr
         (map-indexed
          (fn [i _]
            ^{:key i}
            [:td {:class "text-[#999] text-center px-2 py-1"} (str "L" (bhc/sub i))])
          (:lehmer-digits ex))]
        [:tr
         (map-indexed
          (fn [i d]
            ^{:key i}
            [:td {:class "text-[#ccc] text-center px-2 py-1 tabular-nums"} d])
          (:lehmer-digits ex))]]]]]

    [:div
     [:p {:class "text-sm mb-1"}
      [bhc/step-num 3] "Reconstructed big integer N:"]
     [bhc/code-block (str "N = " (:N ex))]]

    [:div
     [:p {:class "text-sm mb-1"}
      [bhc/step-num 4] "Base-" codec/charset-size " digits d₀–d" (bhc/sub (dec codec/max-chars)) ":"]
     [:div {:class "bg-[#0d0d0d] rounded-lg px-4 py-3 overflow-x-auto mb-4"}
      [:table {:class "text-xs font-mono border-collapse"}
       [:tbody
        [:tr
         (map-indexed
          (fn [i _]
            ^{:key i}
            [:td {:class "text-[#999] text-center px-2 py-1"} (str "d" (bhc/sub i))])
          (:char-indices ex))]
        [:tr
         (map-indexed
          (fn [i d]
            ^{:key i}
            [:td {:class "text-[#ccc] text-center px-2 py-1 tabular-nums"} d])
          (:char-indices ex))]]]]]

    [:div
     [:p {:class "text-sm mb-1"}
      [bhc/step-num 5] "Decoded message:"]
     [bhc/code-block (str "\"" (:text ex) "\"")]]]])
