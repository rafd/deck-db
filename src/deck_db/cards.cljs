(ns deck-db.cards)

;; Canonical 52-card deck ordering: clubs A-K, diamonds A-K, hearts A-K, spades A-K
;; Image filenames use: A, 02–09, 10, J, Q, K

(def suits ["clubs" "diamonds" "hearts" "spades"])
(def values ["A" "02" "03" "04" "05" "06" "07" "08" "09" "10" "J" "Q" "K"])

(def all-cards
  (vec
   (for [suit  suits
         value values]
     {:suit suit :value value})))

;; Index 0  = clubs/A
;; Index 12 = clubs/K
;; Index 13 = diamonds/A
;; Index 51 = spades/K

(defn image-path [{:keys [suit value]}]
  (str "/cards/card_" suit "_" value ".png"))
