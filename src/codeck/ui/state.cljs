(ns codeck.ui.state
  (:require
   [reagent.core :as r]
   [codeck.codec :as codec]
   [codeck.img-codec :as img-codec]))

(defonce *perm (r/atom (vec (range codec/deck-size))))

(defonce *text (r/reaction (codec/decode @*perm)))

(defonce *pixels (r/reaction (img-codec/decode-img @*perm)))

(defn set-perm! [p]
  (reset! *perm p))

(defn set-text! [s]
  (->> s
       codec/sanitize
       codec/encode
       (reset! *perm)))

(defn set-pixels! [pixels]
  (->> pixels
       img-codec/encode-img
       set-perm!))
