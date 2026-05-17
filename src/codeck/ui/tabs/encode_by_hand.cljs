(ns codeck.ui.tabs.encode-by-hand
  (:require
   [clojure.string :as str]
   [reagent.core :as r]
   [codeck.codec :as codec]
   [codeck.ui.tabs.by-hand-common :as bhc]
   [codeck.ui.common :as ui]))

(def *example-text (r/atom "Hello, world!"))
(def *ex (r/reaction (codec/encode-steps @*example-text)))
(def *ex-bigint-steps (r/reaction (codec/text->bigint-steps @*example-text)))
(def *ex-msg-len (r/reaction (count @*example-text)))
(def *ex-pad-len (r/reaction (- codec/max-chars @*ex-msg-len)))

(defn tab []
  [:div {:class (str "text-[" ui/color-text-secondary "] pb-10 font-mono")}

   [:p {:class "mb-3"}
    "A " codec/deck-size "-card deck has " codec/deck-size "! ≈ 8×10⁶⁷ possible orderings. With a character set of " codec/charset-size " characters, that's enough to store a message of up to "
    [:strong {:class "text-white"} codec/max-chars " characters"]
    " (" "log(" codec/deck-size "!)" "/" "log(" codec/charset-size ") ≈ " codec/max-chars ")"

    ". The encoding converts a message into an integer in base " codec/charset-size ", then uses the "
    [:a {:class "underline"
         :href "https://en.wikipedia.org/wiki/Factorial_number_system"} "factorial number system"]
    " to map that integer to a unique card ordering."]

   ;; STEP 1 ---

   [:div {:class "space-y-4"}
    [bhc/step-header "Step 1" "Write Your Message"]

    [:input {:class ui/textarea-class
             :value @*example-text
             :on-change (fn [e]
                          (->> (.. e -target -value)
                               codec/sanitize
                               (reset! *example-text)))}]

    [:p {:class (str "text-[" ui/color-text "]")}
     "Use only characters from the table below. Maximum " codec/max-chars " characters. "
     "Pad to exactly " codec/max-chars " characters by prepending spaces on the left."]

    [bhc/charset-table]]

   ;; STEP 2 ---

   [:div {:class "space-y-4"}
    [bhc/step-header "Step 2" "Convert to Indexes"]

    [:p {:class (str "text-[" ui/color-text "]")}
     "Look up each of your " codec/max-chars " characters in the charset table above. "]

    [ui/blackboard
     [:table {:class "text-xs font-mono border-collapse"}
      [:thead
       [:tr
        [:th {:class (str "text-left text-[" ui/color-text-muted "] pb-2 pr-4 font-normal")} "Char"]
        [:th {:class (str "text-right text-[" ui/color-text-muted "] pb-2 pr-4 font-normal whitespace-nowrap")} "Index"]]]
      [:tbody
       (map-indexed
        (fn [i [c idx]]
          ^{:key i}
          [:tr
           [:td {:class (str "text-[" ui/color-text-secondary "] text-right px-2 py-1")} (bhc/display-char c)]
           [:td {:class (str "text-[" ui/color-text-accent "] text-right px-2 py-1 tabular-nums")} idx]])
        (->> (map vector (:padded @*ex) (:char-indices @*ex))
             (drop-while (fn [[c _]] (= c \space)))))]]]]

   ;; STEP 3 ---

   [:div {:class "space-y-4"}
    [bhc/step-header "Step 3" "Compute the Permutation Number"]
    [:p "Treat the digits as a base-" codec/charset-size " number:"]

    [ui/blackboard
     [:table {:class "text-xs font-mono border-collapse"}
      [:thead
       [:tr
        [:th {:class (str "text-left text-[" ui/color-text-muted "] pb-2 pr-4 font-normal")} "Char"]
        [:th {:class (str "text-right text-[" ui/color-text-muted "] pb-2 pr-4 font-normal whitespace-nowrap")} "Index"]
        [:th]
        [:th {:class (str "text-right text-[" ui/color-text-muted "] pb-2 pr-4 font-normal")} "Power"]
        [:th {:class (str "text-right text-[" ui/color-text-muted "] pb-2 font-normal")} "Value"]]]
      [:tbody
       (for [{:keys [char idx exp contribution]} @*ex-bigint-steps]
         ^{:key exp}
         [:tr
          [:td {:class (str "text-[" ui/color-text-secondary "] py-0.5 pr-4")} (bhc/display-char char)]
          [:td {:class (str "text-right text-[" ui/color-text-secondary "] py-0.5 pr-4 tabular-nums")} idx]
          [:td {:class "py-0.5"} "×"]
          [:td {:class (str "text-right text-[" ui/color-text-accent "] py-0.5 pr-4")}
           (str codec/charset-size (bhc/sup exp))]
          [:td {:class (str "text-[" ui/color-text-secondary "] py-0.5 text-right w-full")} contribution]])]]]

    [ui/blackboard
     [:div {:class "text-xs text-right flex"}
      [:span "Sum = "]
      [:span {:class "grow"}]
      [:span (:N @*ex)]]]]

   ;; STEP 4 ---

   [:div {:class "space-y-4"}
    [bhc/step-header "Step 4" "Convert to a Factoradic Base"]

    [:p {:class "mb-2"}
     "Start with a list of all " codec/deck-size " card indices in order:"]

    [bhc/formula (str "available = [0, 1, 2, 3, …, " (dec codec/deck-size) "]")]

    [:p {:class "mb-1"} "For each of the " codec/deck-size " deck positions (first to last):"]
    [:ol {:class "list-none space-y-1 pl-2 mb-2"}
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
    [:p {:class (str "text-[" ui/color-text-muted "]")}
     "First iteration uses " (dec codec/deck-size) "!, second uses " (- codec/deck-size 2) "!, …, last uses 0! = 1."]

    [ui/blackboard
     [:table {:class "text-xs font-mono border-collapse"}
      [:tbody
       (map-indexed
        (fn [i [d card-idx]]
          ^{:key i}
          [:tr
           [:td {:class (str "text-[" ui/color-text-muted "] text-right px-2 py-1")} (str "L" (bhc/sub i))]
           [:td {:class (str "text-[" ui/color-text-secondary "] text-right px-2 py-1 tabular-nums")} d]])
        (map vector (:lehmer-digits @*ex) (:perm @*ex)))]]]]

   ;; STEP 5 ---

   [:div {:class "space-y-4"}
    [bhc/step-header "Step 5" "Arrange a Physical Deck"]
    [:p {:class ""}
     "Convert each card index back to a card name using the reference table below "
     "and arrange the deck."]

    [bhc/card-index-table]

    [ui/blackboard
     [:table {:class "text-xs font-mono border-collapse"}
      [:tbody
       (map-indexed
        (fn [i [d card-idx]]
          ^{:key i}
          [:tr
           [:td {:class (str "text-[" ui/color-text-secondary "] px-2 py-1 tabular-nums text-right")} d]
           [:td {:class (str "text-[" ui/color-text-accent "] px-2 py-1 text-right")} (bhc/idx->card-name card-idx)]])
        (map vector (:lehmer-digits @*ex) (:perm @*ex)))]]]]])
