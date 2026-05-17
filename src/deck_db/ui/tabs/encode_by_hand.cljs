(ns deck-db.tabs.encode-by-hand
  (:require
   [clojure.string :as str]
   [deck-db.codec :as codec]
   [deck-db.tabs.by-hand-common :as bhc]
   [deck-db.ui.common :as ui]))

(def ^:private example-text "Hello, world!")
(def ^:private ex (codec/encode-steps example-text))
(def ^:private ex-msg-len (count example-text))
(def ^:private ex-pad-len (- codec/max-chars ex-msg-len))

(defn tab []
  [:div {:class (str "text-[" ui/color-text-secondary "] pb-10 font-mono")}

   [bhc/section-header "How to Encode a Message by Hand"]

   [:p {:class "text-sm mb-3"}
    "A " codec/deck-size "-card deck has " codec/deck-size "! ≈ 8×10⁶⁷ possible orderings. With a character set of " codec/charset-size ", that's enough to store a message of up to "
    [:strong {:class "text-white"} codec/max-chars " characters"]
    ". The encoding converts your message into a big integer in base " codec/charset-size ", then uses the "
    [:em "factoradic"] " (factorial number system) to map that integer to a unique card ordering."]

   [bhc/section-header "Reference: Character → Index"]
   [:p {:class (str "text-[" ui/color-text-muted "] text-xs mb-2")}
    codec/charset-size " characters, indices 0–" (dec codec/charset-size) ". Space is shown as ·"]
   [bhc/charset-table]

   [bhc/section-header "Reference: Card → Index"]
   [bhc/card-index-table]

   [bhc/section-header "Steps"]

   [:div {:class "space-y-5"}
    [:div
     [:p {:class "text-sm mb-1"}
      [bhc/step-num 1] "Write out your message"]
     [:p {:class (str "text-sm text-[" ui/color-text-muted "]")}
      "Use only characters from the table above. Maximum " codec/max-chars " characters. "
      "Pad to exactly " codec/max-chars " characters by appending spaces on the right."]]

    [:div
     [:p {:class "text-sm mb-1"}
      [bhc/step-num 2] "Convert each character to its index"]
     [:p {:class "text-sm mb-1"}
      "Look up each of your " codec/max-chars " characters in the charset table above. "
      "Call the resulting numbers d₀, d₁, …, d" (bhc/sub (dec codec/max-chars))
      " (each is 0–" (dec codec/charset-size) "). "
      "Padded spaces become 0."]
     [:p {:class (str "text-sm text-[" ui/color-text-muted "]")}
      "Example: 'H' → " (get codec/char->idx \H)
      ",  'i' → " (get codec/char->idx \i)
      ",  ' ' → 0"]]

    [:div
     [:p {:class "text-sm mb-1"}
      [bhc/step-num 3] "Build a big integer N from those " codec/max-chars " digits"]
     [:p {:class "text-sm mb-2"}
      "Treat the digits as a base-" codec/charset-size " number, most significant first:"]
     [bhc/formula (str "N = d₀ × " codec/charset-size (bhc/sup (dec codec/max-chars))
                       "  +  d₁ × " codec/charset-size (bhc/sup (- codec/max-chars 2))
                       "  +  …  +  d" (bhc/sub (dec codec/max-chars))
                       " × " codec/charset-size "⁰")]
     [:p {:class (str "text-sm text-[" ui/color-text-muted "]")}
      "N can reach ~8×10⁶⁷."]]

    [:div
     [:p {:class "text-sm mb-1"}
      [bhc/step-num 4] "Convert N to a card ordering (factoradic)"]
     [:p {:class "text-sm mb-2"}
      "Start with a list of all " codec/deck-size " card indices in order:"]
     [bhc/formula (str "available = [0, 1, 2, 3, …, " (dec codec/deck-size) "]")]
     [:p {:class "text-sm mb-1"} "For each of the " codec/deck-size " deck positions (first to last):"]
     [:ol {:class "text-sm list-none space-y-1 pl-2 mb-2"}
      [:li [:span {:class (str "text-[" ui/color-highlight "]")} "a. "]
       "Divide N by (remaining_count − 1)!  →  write down quotient d and remainder r"]
      [:li [:span {:class (str "text-[" ui/color-highlight "]")} "b. "]
       "The card at position d (0-indexed) in "
       [:code {:class (str "text-[" ui/color-text-accent "] font-mono text-xs")} "available"]
       " goes next in your deck"]
      [:li [:span {:class (str "text-[" ui/color-highlight "]")} "c. "]
       "Remove that card from "
       [:code {:class (str "text-[" ui/color-text-accent "] font-mono text-xs")} "available"]]
      [:li [:span {:class (str "text-[" ui/color-highlight "]")} "d. "]
       "Set N = r and repeat for the next position"]]
     [:p {:class (str "text-sm text-[" ui/color-text-muted "]")}
      "First iteration uses " (dec codec/deck-size) "!, second uses " (- codec/deck-size 2) "!, …, last uses 0! = 1."]]

    [:div
     [:p {:class "text-sm mb-1"}
      [bhc/step-num 5] "Arrange a physical deck in that order"]
     [:p {:class "text-sm"}
      "Convert each card index back to a card name using the reference table above "
      "and arrange the deck. That ordering now encodes your message."]]]

   [bhc/section-header "Example: \"Hello, world!\""]

   [:div {:class "space-y-5"}
    [:div
     [:p {:class "text-sm mb-1"}
      [bhc/step-num 1] "Padded to " codec/max-chars " characters (+" ex-pad-len " spaces):"]
     [bhc/code-block (apply str (map bhc/display-char (:padded ex)))]]

    [:div
     [:p {:class "text-sm mb-1"}
      [bhc/step-num 2] "Character → index:"]
     [:div {:class (str "bg-[" ui/color-bg-surface "] rounded-lg px-4 py-3 overflow-x-auto mb-4")}
      [:table {:class "text-xs font-mono border-collapse"}
       [:tbody
        [:tr
         (map-indexed
          (fn [i c]
            ^{:key i}
            [:td {:class (str "text-[" ui/color-text-secondary "] text-center px-2 py-1")} (bhc/display-char c)])
          (:padded ex))]
        [:tr
         (map-indexed
          (fn [i idx]
            ^{:key i}
            [:td {:class (str "text-[" ui/color-text-accent "] text-center px-2 py-1 tabular-nums")} idx])
          (:char-indices ex))]]]]]

    [:div
     [:p {:class "text-sm mb-1"}
      [bhc/step-num 3] "Big integer N:"]
     [bhc/code-block (str "N = " (:N ex))]]

    [:div
     [:p {:class "text-sm mb-1"}
      [bhc/step-num 4] "Factoradic → card ordering:"]
     [:div {:class (str "bg-[" ui/color-bg-surface "] rounded-lg px-4 py-3 overflow-x-auto mb-4")}
      [:table {:class "text-xs font-mono border-collapse"}
       [:tbody
        [:tr
         (map-indexed
          (fn [i _]
            ^{:key i}
            [:td {:class (str "text-[" ui/color-text-muted "] text-center px-2 py-1")} (str "L" (bhc/sub i))])
          (range codec/deck-size))]
        [:tr
         (map-indexed
          (fn [i d]
            ^{:key i}
            [:td {:class (str "text-[" ui/color-text-secondary "] text-center px-2 py-1 tabular-nums")} d])
          (:lehmer-digits ex))]
        [:tr
         (map-indexed
          (fn [i card-idx]
            ^{:key i}
            [:td {:class (str "text-[" ui/color-text-accent "] text-center px-2 py-1")} (bhc/idx->card-name card-idx)])
          (:perm ex))]]]]]

    [:div
     [:p {:class "text-sm mb-1"}
      [bhc/step-num 5] "Full deck ordering:"]
     [bhc/code-block (str/join "  " (map bhc/idx->card-name (:perm ex)))]]]])
