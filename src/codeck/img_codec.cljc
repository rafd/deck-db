(ns codeck.img-codec)

;; 15×15 pixel grid → 225 bits
;; 2^225 < 52! ≈ 2^225.58, so any 225-bit value fits in a permutation.

(def img-size 15)
(def pixel-count (* img-size img-size))
(def deck-size 52)

;; BigInt helpers
#?(:clj
   (do
     (defn- bi [n] (biginteger n))
     (defn- bi+ [a b] (.add a b))
     (defn- bi* [a b] (.multiply a b))
     (defn- bi-mod [a b] (.mod a b))
     (defn- bi-div [a b] (.divide a b))
     (defn- bi->int [n] (.intValue n)))
   :cljs
   (do
     (defn- bi [n] (js/BigInt n))
     (defn- bi+ [a b] (js* "~{} + ~{}" a b))
     (defn- bi* [a b] (js* "~{} * ~{}" a b))
     (defn- bi-mod [a b] (js* "~{} % ~{}" a b))
     (defn- bi-div [a b] (js* "~{} / ~{}" a b))
     (defn- bi->int [n] (js/Number n))))

(def ^:private factorials
  (reduce
   (fn [acc k]
     (conj acc (bi* (peek acc) (bi k))))
   [(bi 1)]
   (range 1 (inc deck-size))))

(defn- find-idx [v x]
  (loop [i 0]
    (cond
      (= i (count v)) -1
      (= (nth v i) x) i
      :else (recur (inc i)))))

(defn- bigint->perm [N]
  (loop [n N
         avail (vec (range deck-size))
         perm []]
    (if (empty? avail)
      perm
      (let [k (count avail)
            fact (nth factorials (dec k))
            d (bi->int (bi-div n fact))
            rest-n (bi-mod n fact)]
        (recur rest-n
               (into [] (concat (subvec avail 0 d) (subvec avail (inc d))))
               (conj perm (nth avail d)))))))

(defn- perm->bigint [perm]
  (loop [n (bi 0)
         avail (vec (range deck-size))
         remaining (seq perm)]
    (if (nil? remaining)
      n
      (let [card (first remaining)
            k (count avail)
            fact (nth factorials (dec k))
            d (find-idx avail card)
            new-avail (into [] (concat (subvec avail 0 d) (subvec avail (inc d))))]
        (recur (bi+ n (bi* (bi d) fact))
               new-avail
               (next remaining))))))

(defn- bits->bigint [bits]
  (reduce
   (fn [acc bit]
     (bi+ (bi* acc (bi 2)) (bi bit)))
   (bi 0)
   bits))

(defn- bigint->bits [N]
  (let [raw (loop [n N
                   acc []]
              (if (= (count acc) pixel-count)
                acc
                (recur (bi-div n (bi 2))
                       (conj acc (bi->int (bi-mod n (bi 2)))))))]
    (vec (reverse raw))))

;; Public API

(defn encode-img
  "Encode a flat vector of pixel-count bits (0/1, row-major) into a permutation of 52 card indices."
  [bits]
  (-> bits bits->bigint bigint->perm))

(defn decode-img
  "Decode a permutation of 52 card indices into a flat vector of pixel-count bits (0/1, row-major)."
  [perm]
  (-> perm perm->bigint bigint->bits))

#_(encode-img (vec (repeat pixel-count 0)))
#_(decode-img (encode-img (vec (repeat pixel-count 1))))
