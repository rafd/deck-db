(ns deck-db.panels.encode-by-hand
  (:require
   [deck-db.codec :as codec]
   [deck-db.panels.by-hand-common :as bhc]))

(defn panel []
  [:div {:class "text-[#ccc] pb-10 font-mono"}

   [bhc/section-header "How to Encode a Message by Hand"]

   [:p {:class "text-sm mb-3"}
    "A 52-card deck has 52! ≈ 8×10⁶⁷ possible orderings — enough to store up to "
    [:strong {:class "text-white"} codec/max-chars " characters"]
    ". The encoding converts your message into a big integer in base 64, then uses the "
    [:em "factoradic"] " (factorial number system) to map that integer to a unique card ordering."]

   [bhc/section-header "Reference: Character → Index"]
   [:p {:class "text-[#999] text-xs mb-2"} "64 characters, indices 0–63. Space is shown as ·"]
   [bhc/charset-table]

   [bhc/section-header "Reference: Card → Index"]
   [bhc/card-index-table]

   [bhc/section-header "Steps"]

   [:div {:class "mb-5"}
    [:p {:class "text-sm mb-1"}
     [bhc/step-num 1] "Write out your message"]
    [:p {:class "text-sm text-[#999]"}
     "Use only: A–Z, a–z, 0–9, space, apostrophe. Maximum " codec/max-chars " characters. "
     "Pad to exactly " codec/max-chars " characters by appending spaces on the right."]]

   [:div {:class "mb-5"}
    [:p {:class "text-sm mb-1"}
     [bhc/step-num 2] "Convert each character to its index"]
    [:p {:class "text-sm mb-1"}
     "Look up each of your " codec/max-chars " characters in the charset table above. "
     "Call the resulting numbers d₀, d₁, …, d₃₆ (each is 0–63). "
     "Padded spaces become 0."]
    [:p {:class "text-sm text-[#999]"} "Example: 'H' → 19,  'i' → 45,  ' ' → 0"]]

   [:div {:class "mb-5"}
    [:p {:class "text-sm mb-1"}
     [bhc/step-num 3] "Build a big integer N from those " codec/max-chars " digits"]
    [:p {:class "text-sm mb-2"}
     "Treat the digits as a base-64 number, most significant first:"]
    [bhc/formula "N = d₀ × 64³⁶  +  d₁ × 64³⁵  +  …  +  d₃₆ × 64⁰"]
    [:p {:class "text-sm text-[#999]"}
     "N can reach ~8×10⁶⁷."]]

   [:div {:class "mb-5"}
    [:p {:class "text-sm mb-1"}
     [bhc/step-num 4] "Convert N to a card ordering (factoradic)"]
    [:p {:class "text-sm mb-2"}
     "Start with a list of all 52 card indices in order:"]
    [bhc/formula "available = [0, 1, 2, 3, …, 51]"]
    [:p {:class "text-sm mb-1"} "For each of the 52 deck positions (first to last):"]
    [:ol {:class "text-sm list-none space-y-1 pl-2 mb-2"}
     [:li [:span {:class "text-[#c8a84b]"} "a. "]
      "Divide N by (remaining_count − 1)!  →  write down quotient d and remainder r"]
     [:li [:span {:class "text-[#c8a84b]"} "b. "]
      "The card at position d (0-indexed) in "
      [:code {:class "text-[#7ea974] font-mono text-xs"} "available"]
      " goes next in your deck"]
     [:li [:span {:class "text-[#c8a84b]"} "c. "]
      "Remove that card from "
      [:code {:class "text-[#7ea974] font-mono text-xs"} "available"]]
     [:li [:span {:class "text-[#c8a84b]"} "d. "]
      "Set N = r and repeat for the next position"]]
    [:p {:class "text-sm text-[#999]"}
     "First iteration uses 51!, second uses 50!, …, last uses 0! = 1."]]

   [:div {:class "mb-5"}
    [:p {:class "text-sm mb-1"}
     [bhc/step-num 5] "Arrange a physical deck in that order"]
    [:p {:class "text-sm"}
     "Convert each card index back to a card name using the reference table above "
     "and arrange the deck. That ordering now encodes your message."]]])
