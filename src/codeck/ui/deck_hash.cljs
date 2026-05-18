(ns codeck.ui.deck-hash
  (:require
   [clojure.string :as str]
   [codeck.cards :as cards]
   [codeck.ui.common :as ui]))

(def ^:private rank->card-idx
  {"a" 0 "2" 1 "3" 2 "4" 3 "5" 4 "6" 5 "7" 6 "8" 7 "9" 8 "10" 9 "j" 10 "q" 11 "k" 12})

(def ^:private suit->suit-idx
  {"c" 0 "d" 1 "h" 2 "s" 3})

(defn- card->str [{:keys [value suit]}]
  (let [rank (case value
               "02" "2" "03" "3" "04" "4" "05" "5"
               "06" "6" "07" "7" "08" "8" "09" "9"
               value)]
    (str rank (first suit))))

(defn perm->str [perm]
  (->> perm
       (map (fn [idx] (card->str (nth cards/all-cards idx))))
       (apply str)))

(defn- parse-tokens [s]
  (loop [remaining s acc []]
    (if (empty? remaining)
      acc
      (let [is-ten?    (str/starts-with? remaining "10")
            rank       (if is-ten? "10" (subs remaining 0 1))
            after-rank (subs remaining (if is-ten? 2 1))]
        (if (empty? after-rank)
          nil
          (recur (subs after-rank 1)
                 (conj acc [rank (subs after-rank 0 1)])))))))

(defn str->perm [s]
  (let [cleaned (-> s str/lower-case (str/replace #"\s+" ""))
        tokens  (parse-tokens cleaned)]
    (when (and tokens (= (count tokens) 52))
      (let [indices (mapv (fn [[rank suit]]
                            (let [r (rank->card-idx rank)
                                  s (suit->suit-idx suit)]
                              (when (and r s)
                                (+ (* s 13) r))))
                          tokens)]
        (when (every? some? indices)
          (let [perm (vec indices)]
            (when (= (set perm) (set (range 52)))
              perm)))))))



(defn in-view [{:keys [*string *deck read-only? on-perm-change!]}]
  [:textarea
   {:class (str "w-full font-mono text-sm bg-[" ui/color-bg-dark "] text-[" ui/color-text-accent "] "
                "rounded-md p-2 resize-none focus:outline-none break-all")
    :rows 3
    :read-only read-only?
    :value @*string
    :on-change (fn [e]
                 (let [v (.. e -target -value)]
                   (reset! *string v)
                   (when-let [p (str->perm v)]
                     (reset! *deck p)
                     (when on-perm-change! (on-perm-change! p)))))}])

(defn out-view [perm]
  [in-view {:read-only? true
            :*string (atom (perm->str perm))}])
