(ns codeck.codec
  (:require
   [clojure.string :as str]
   [codeck.perm :as perm]))

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

;; Largest n such that (count charset)^n ≤ deck-size!
;; = floor( log(deck-size!) / log(charset-size) )
;; log(deck-size!) is computed as a sum to avoid materialising the BigInt.
(def max-chars
  (let [log-deck-perms (reduce + (map (fn [x] (Math/log x)) (range 1 (inc deck-size))))
        log-base (Math/log charset-size)]
    (int (Math/floor (/ log-deck-perms log-base)))))

;; base-powers[k] = charset-size^k  (indices 0..max-chars)
(def ^:private base-powers
  (reduce
   (fn [acc _]
     (conj acc (perm/bi* (peek acc) (perm/bi charset-size))))
   [(perm/bi 1)]
   (range max-chars)))

;; Encode text as a base-(count charset) big integer (max-chars digits, space-padded)
(defn- text->bigint [text]
  (let [base (perm/bi (count charset))
        s (str (apply str (repeat max-chars " ")) text)
        padded (subs s (- (count s) max-chars))]
    (reduce
     (fn [acc ch]
       (perm/bi+ (perm/bi* acc base) (perm/bi (get char->idx ch 0))))
     (perm/bi 0)
     padded)))

;; Convert big integer back to text (reverse of text->bigint)
(defn- bigint->text [N]
  (let [base (perm/bi (count charset))
        digits (loop [n N
                      acc []]
                 (if (= (count acc) max-chars)
                   acc
                   (recur (perm/bi-div n base)
                          (conj acc (perm/bi->int (perm/bi-mod n base))))))]
    (->> digits
         reverse
         (map #(nth idx->char %))
         (apply str)
         str/triml)))

(defn- bi->str [n]
  #?(:clj  (str n)
     :cljs (js/String n)))

;; Public API

(defn encode
  "Encode text (up to max-chars chars from ASCII 32–126) into a permutation of
  card indices 0–(deck-size-1). Returns a vector of deck-size integers."
  [text]
  (-> text text->bigint perm/bigint->perm))

(defn decode
  "Decode a permutation of card indices into text.
  Returns a string with trailing spaces stripped."
  [p]
  (-> p perm/perm->bigint bigint->text))

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
  (let [s (str (apply str (repeat max-chars " ")) text)
        padded (subs s (- (count s) max-chars))
        char-idxs (mapv (fn [ch] (get char->idx ch 0)) padded)
        N (text->bigint text)]
    (loop [n N
           avail (vec (range deck-size))
           lehmer []
           p []]
      (if (empty? avail)
        {:padded padded
         :char-indices char-idxs
         :N (bi->str N)
         :lehmer-digits lehmer
         :perm p}
        (let [k (count avail)
              fact (nth perm/factorials (dec k))
              d (perm/bi->int (perm/bi-div n fact))
              rest-n (perm/bi-mod n fact)]
          (recur rest-n
                 (into [] (concat (subvec avail 0 d) (subvec avail (inc d))))
                 (conj lehmer d)
                 (conj p (nth avail d))))))))

(defn decode-steps
  "Return intermediate values for decoding a permutation.
   Keys: :lehmer-digits :N :char-indices :text"
  [p]
  (let [{:keys [N lehmer]}
        (loop [n (perm/bi 0)
               avail (vec (range deck-size))
               remaining (seq p)
               lehmer []]
          (if (nil? remaining)
            {:N n :lehmer lehmer}
            (let [card (first remaining)
                  k (count avail)
                  fact (nth perm/factorials (dec k))
                  d (perm/find-idx avail card)
                  new-avail (into [] (concat (subvec avail 0 d) (subvec avail (inc d))))]
              (recur (perm/bi+ n (perm/bi* (perm/bi d) fact))
                     new-avail
                     (next remaining)
                     (conj lehmer d)))))
        base (perm/bi (count charset))
        digits (loop [n N
                      acc []]
                 (if (= (count acc) max-chars)
                   acc
                   (recur (perm/bi-div n base)
                          (conj acc (perm/bi->int (perm/bi-mod n base))))))
        char-idxs (vec (reverse digits))
        text (->> (map #(nth idx->char %) char-idxs)
                  (apply str)
                  str/triml)]
    {:lehmer-digits lehmer
     :N (bi->str N)
     :char-indices char-idxs
     :text text}))

(defn text->bigint-steps
  "Return per-character contribution data for the text→BigInt conversion.
   One entry per character in text (not padding).
   Each map: {:char :idx :exp :contribution}"
  [text]
  (vec
   (map-indexed
    (fn [i ch]
      (let [idx (get char->idx ch 0)
            exp (- (count text) 1 i)
            contrib (perm/bi* (perm/bi idx) (nth base-powers exp))]
        {:char ch
         :idx idx
         :exp exp
         :contribution (bi->str contrib)}))
    text)))

#_(encode "Hello World")
#_(decode (encode "Hello World"))
#_(encode-steps "Hello, world!")
#_(decode-steps (encode "Hello, world!"))
#_(text->bigint-steps "H")
