(ns deck-db.panels.decode-by-hand
  (:require
   [deck-db.panels.by-hand-common :as bhc]))

(defn panel []
  [:div {:class "text-[#ccc] pb-10 font-mono"}

   [bhc/section-header "How to Decode a Deck by Hand"]

   [:p {:class "text-sm mb-3"}
    "Given a deck of 52 cards in a specific order, this process extracts the hidden message. "
    "It reverses the encoding: convert the card ordering into a big integer using the "
    [:em "factoradic"] " system, then convert that integer from base 64 back into characters."]

   [bhc/section-header "Reference: Card → Index"]
   [bhc/card-index-table]

   [bhc/section-header "Reference: Index → Character"]
   [:p {:class "text-[#999] text-xs mb-2"} "64 characters total (indices 0–63)"]
   [bhc/charset-table]

   [bhc/section-header "Steps"]

   [:div {:class "mb-5"}
    [:p {:class "text-sm mb-1"}
     [bhc/step-num 1] "Number each card in your deck"]
    [:p {:class "text-sm mb-1"}
     "Look up every card in the reference table above and write down its index (0–51). "
     "You should end up with a sequence of 52 distinct numbers."]
    [:p {:class "text-sm text-[#999]"} "Example start: A♣=0, K♠=51, 7♥=32, …"]]

   [:div {:class "mb-5"}
    [:p {:class "text-sm mb-1"}
     [bhc/step-num 2] "Compute the Lehmer code (factoradic digits)"]
    [:p {:class "text-sm mb-2"}
     "Start with a list of all 52 indices in order:"]
    [bhc/formula "available = [0, 1, 2, 3, …, 51]"]
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
     "You'll get 52 Lehmer digits L₀–L₅₁. "
     "L₀ is in 0–51, L₁ is in 0–50, …, L₅₁ is always 0."]]

   [:div {:class "mb-5"}
    [:p {:class "text-sm mb-1"}
     [bhc/step-num 3] "Reconstruct the big integer N"]
    [:p {:class "text-sm mb-2"}
     "Combine the Lehmer digits using factorials:"]
    [bhc/formula "N = L₀ × 51!  +  L₁ × 50!  +  …  +  L₅₁ × 0!"]
    [:p {:class "text-sm text-[#999]"}
     "N can be up to ~8×10⁶⁷."]]

   [:div {:class "mb-5"}
    [:p {:class "text-sm mb-1"}
     [bhc/step-num 4] "Convert N to 37 base-64 digits"]
    [:p {:class "text-sm mb-2"}
     "Repeatedly divide N by 64, collecting remainders — this gives digits least-significant first. "
     "Repeat exactly 37 times, then reverse the list:"]
    [:ol {:class "text-sm list-none space-y-1 pl-2 mb-2"}
     [:li [:span {:class "text-[#c8a84b]"} "a. "]
      "digit = N mod 64,  N = N ÷ 64"]
     [:li [:span {:class "text-[#c8a84b]"} "b. "]
      "Repeat 37 times, collecting each digit"]
     [:li [:span {:class "text-[#c8a84b]"} "c. "]
      "Reverse the collected digits — now d₀ is most significant"]]
    [:p {:class "text-sm text-[#999]"}
     "You now have 37 numbers d₀–d₃₆, each in 0–63."]]

   [:div {:class "mb-5"}
    [:p {:class "text-sm mb-1"}
     [bhc/step-num 5] "Convert each digit to a character"]
    [:p {:class "text-sm mb-1"}
     "Look up each digit in the charset table above to get your 37 characters."]
    [:p {:class "text-sm text-[#999]"}
     "Trim any trailing spaces — that is your decoded message."]]])
