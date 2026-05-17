(ns codeck.perm)

;; BigInt helpers — large-integer arithmetic needed for deck-size! (≈ 2^225)
#?(:clj
   (do
     (defn bi [n] (biginteger n))
     (defn bi+ [a b] (.add a b))
     (defn bi* [a b] (.multiply a b))
     (defn bi-mod [a b] (.mod a b))
     (defn bi-div [a b] (.divide a b))
     (defn bi->int [n] (.intValue n))
     (defn bi> [a b] (pos? (.compareTo a b))))
   :cljs
   (do
     (defn bi [n] (js/BigInt n))
     (defn bi+ [a b] (js* "~{} + ~{}" a b))
     (defn bi* [a b] (js* "~{} * ~{}" a b))
     (defn bi-mod [a b] (js* "~{} % ~{}" a b))
     (defn bi-div [a b] (js* "~{} / ~{}" a b))
     (defn bi->int [n] (js/Number n))
     (defn bi> [a b] (js* "~{} > ~{}" a b))))

(def ^:private deck-size 52)

;; factorials[k] = k!  (indices 0..deck-size)
(def factorials
  (reduce
   (fn [acc k]
     (conj acc (bi* (peek acc) (bi k))))
   [(bi 1)]
   (range 1 (inc deck-size))))

(defn find-idx
  "Linear search: index of x in vector v, or -1."
  [v x]
  (loop [i 0]
    (cond
      (= i (count v)) -1
      (= (nth v i) x) i
      :else (recur (inc i)))))

(defn bigint->perm [N]
  (loop [n N
         avail (vec (range deck-size))
         p []]
    (if (empty? avail)
      p
      (let [k (count avail)
            fact (nth factorials (dec k))
            d (bi->int (bi-div n fact))
            rest-n (bi-mod n fact)]
        (recur rest-n
               (into [] (concat (subvec avail 0 d) (subvec avail (inc d))))
               (conj p (nth avail d)))))))

(defn perm->bigint [p]
  (loop [n (bi 0)
         avail (vec (range deck-size))
         remaining (seq p)]
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
