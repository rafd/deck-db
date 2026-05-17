(ns deck-db.codec
  (:require
   [clojure.string :as str]))

;; ASCII 32–126: all non-control characters (space through ~)  →  95 characters
(def charset
  #_(apply str (map char (range 32 127)))
  " !\"#$%&'()*+,-./0123456789:;<=>?@ABCDEFGHIJKLMNOPQRSTUVWXYZ[\\]^_`abcdefghijklmnopqrstuvwxyz{|}~")

(def valid-chars (set charset))
(def charset-size (count charset))
(def deck-size 52)

(def char->idx
  (into {} (map-indexed (fn [i c] [c i]) charset)))

(def idx->char (vec charset))

;; BigInt helpers — large-integer arithmetic needed for deck-size! (≈ 2^225)
#?(:clj
   (do
     (defn- bi [n] (biginteger n))
     (defn- bi+ [a b] (.add a b))
     (defn- bi* [a b] (.multiply a b))
     (defn- bi-mod [a b] (.mod a b))
     (defn- bi-div [a b] (.divide a b))
     (defn- bi->int [n] (.intValue n))
     (defn- bi> [a b] (pos? (.compareTo a b))))
   :cljs
   (do
     (defn- bi [n] (js/BigInt n))
     (defn- bi+ [a b] (js* "~{} + ~{}" a b))
     (defn- bi* [a b] (js* "~{} * ~{}" a b))
     (defn- bi-mod [a b] (js* "~{} % ~{}" a b))
     (defn- bi-div [a b] (js* "~{} / ~{}" a b))
     (defn- bi->int [n] (js/Number n))
     (defn- bi> [a b] (js* "~{} > ~{}" a b))))

;; factorials[k] = k!  (indices 0..deck-size)
(def factorials
  (reduce
   (fn [acc k]
     (conj acc (bi* (peek acc) (bi k))))
   [(bi 1)]
   (range 1 (inc deck-size))))

;; Largest n such that (count charset)^n ≤ deck-size!
;; (each charset character encodes log2(count charset) bits; deck-size! permutations bound the capacity)
(def max-chars
  (let [deck-perms (nth factorials deck-size)
        base       (bi (count charset))]
    (loop [n     0
           power (bi 1)]
      (let [next-power (bi* power base)]
        (if (bi> next-power deck-perms)
          n
          (recur (inc n) next-power))))))

(defn- find-idx
  "Linear search: index of x in vector v, or -1."
  [v x]
  (loop [i 0]
    (cond
      (= i (count v)) -1
      (= (nth v i) x) i
      :else (recur (inc i)))))

;; Encode text as a base-(count charset) big integer (max-chars digits, space-padded)
(defn- text->bigint [text]
  (let [base   (bi (count charset))
        padded (subs (str text (apply str (repeat max-chars " "))) 0 max-chars)]
    (reduce
     (fn [acc ch]
       (bi+ (bi* acc base) (bi (get char->idx ch 0))))
     (bi 0)
     padded)))

;; Convert big integer to a permutation of [0..deck-size-1] via Lehmer/factoradic
(defn- bigint->perm [N]
  (loop [n     N
         avail (vec (range deck-size))
         perm  []]
    (if (empty? avail)
      perm
      (let [k      (count avail)
            fact   (nth factorials (dec k))
            d      (bi->int (bi-div n fact))
            rest-n (bi-mod n fact)]
        (recur rest-n
               (into [] (concat (subvec avail 0 d) (subvec avail (inc d))))
               (conj perm (nth avail d)))))))

;; Convert a permutation back to a big integer
(defn- perm->bigint [perm]
  (loop [n         (bi 0)
         avail     (vec (range deck-size))
         remaining (seq perm)]
    (if (nil? remaining)
      n
      (let [card      (first remaining)
            k         (count avail)
            fact      (nth factorials (dec k))
            d         (find-idx avail card)
            new-avail (into [] (concat (subvec avail 0 d) (subvec avail (inc d))))]
        (recur (bi+ n (bi* (bi d) fact))
               new-avail
               (next remaining))))))

;; Convert big integer back to text (reverse of text->bigint)
(defn- bigint->text [N]
  (let [base   (bi (count charset))
        digits (loop [n   N
                      acc []]
                 (if (= (count acc) max-chars)
                   acc
                   (recur (bi-div n base)
                          (conj acc (bi->int (bi-mod n base))))))]
    (->> digits
         reverse
         (map #(nth idx->char %))
         (apply str)
         str/trimr)))

(defn- bi->str [n]
  #?(:clj  (str n)
     :cljs (js/String n)))

;; Public API

(defn encode
  "Encode text (up to max-chars chars from ASCII 32–126) into a permutation of
  card indices 0–(deck-size-1). Returns a vector of deck-size integers."
  [text]
  (-> text text->bigint bigint->perm))

(defn decode
  "Decode a permutation of card indices into text.
  Returns a string with trailing spaces stripped."
  [perm]
  (-> perm perm->bigint bigint->text))

(defn sanitize
  "Remove characters outside the valid charset and truncate to max-chars."
  [text]
  (->> text
       (filter valid-chars)
       (take max-chars)
       (apply str)))

(defn encode-steps
  "Return intermediate values for encoding text.
   Keys: :padded :char-indices :N :lehmer-digits :perm"
  [text]
  (let [padded    (subs (str text (apply str (repeat max-chars " "))) 0 max-chars)
        char-idxs (mapv (fn [ch] (get char->idx ch 0)) padded)
        N         (text->bigint text)]
    (loop [n      N
           avail  (vec (range deck-size))
           lehmer []
           perm   []]
      (if (empty? avail)
        {:padded padded
         :char-indices char-idxs
         :N (bi->str N)
         :lehmer-digits lehmer
         :perm perm}
        (let [k      (count avail)
              fact   (nth factorials (dec k))
              d      (bi->int (bi-div n fact))
              rest-n (bi-mod n fact)]
          (recur rest-n
                 (into [] (concat (subvec avail 0 d) (subvec avail (inc d))))
                 (conj lehmer d)
                 (conj perm (nth avail d))))))))

(defn decode-steps
  "Return intermediate values for decoding a permutation.
   Keys: :lehmer-digits :N :char-indices :text"
  [perm]
  (let [{:keys [N lehmer]}
        (loop [n         (bi 0)
               avail     (vec (range deck-size))
               remaining (seq perm)
               lehmer    []]
          (if (nil? remaining)
            {:N n :lehmer lehmer}
            (let [card      (first remaining)
                  k         (count avail)
                  fact      (nth factorials (dec k))
                  d         (find-idx avail card)
                  new-avail (into [] (concat (subvec avail 0 d) (subvec avail (inc d))))]
              (recur (bi+ n (bi* (bi d) fact))
                     new-avail
                     (next remaining)
                     (conj lehmer d)))))
        base      (bi (count charset))
        digits    (loop [n   N
                         acc []]
                    (if (= (count acc) max-chars)
                      acc
                      (recur (bi-div n base)
                             (conj acc (bi->int (bi-mod n base))))))
        char-idxs (vec (reverse digits))
        text      (->> (map #(nth idx->char %) char-idxs)
                       (apply str)
                       str/trimr)]
    {:lehmer-digits lehmer
     :N (bi->str N)
     :char-indices char-idxs
     :text text}))

#_(encode "Hello World")
#_(decode (encode "Hello World"))
#_(encode-steps "Hello, world!")
#_(decode-steps (encode "Hello, world!"))
