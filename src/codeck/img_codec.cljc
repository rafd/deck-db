(ns codeck.img-codec
  (:require
   [codeck.perm :as perm]))

;; 15×15 pixel grid → 225 bits
;; 2^225 < 52! ≈ 2^225.58, so any 225-bit value fits in a permutation.

(def img-size 15)
(def pixel-count (* img-size img-size))
(def deck-size 52)

(defn- bits->bigint [bits]
  (reduce
   (fn [acc bit]
     (perm/bi+ (perm/bi* acc (perm/bi 2)) (perm/bi bit)))
   (perm/bi 0)
   bits))

(defn- bigint->bits [N]
  (let [raw (loop [n N
                   acc []]
              (if (= (count acc) pixel-count)
                acc
                (recur (perm/bi-div n (perm/bi 2))
                       (conj acc (perm/bi->int (perm/bi-mod n (perm/bi 2)))))))]
    (vec (reverse raw))))

;; Public API

(defn encode-img
  "Encode a flat vector of pixel-count bits (0/1, row-major) into a permutation of 52 card indices."
  [bits]
  (-> bits bits->bigint perm/bigint->perm))

(defn decode-img
  "Decode a permutation of 52 card indices into a flat vector of pixel-count bits (0/1, row-major)."
  [p]
  (-> p perm/perm->bigint bigint->bits))

#_(encode-img (vec (repeat pixel-count 0)))
#_(decode-img (encode-img (vec (repeat pixel-count 1))))
