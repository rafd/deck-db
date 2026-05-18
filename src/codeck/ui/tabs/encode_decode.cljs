(ns codeck.ui.tabs.encode-decode
  (:require
   [reagent.core :as r]
   [codeck.codec :as codec]
   [codeck.ui.common :as ui]
   [codeck.ui.deck-hash :as h]))

(defonce *perm (r/atom (vec (range codec/deck-size))))
(defonce *text (r/atom ""))
(defonce *deck-str (r/atom (h/perm->str (vec (range codec/deck-size)))))

(defn- char-counter []
  (let [n (count @*text)
        limit codec/max-chars
        over? (> n limit)]
    [:div
     {:class (str "text-right tabular-nums "
                  (when over? "text-red-400"))}
     [:span n " / " limit]]))

(defn tab []
  [:div {:class "space-y-4"}
   [:div {:class (str "text-xl text-[" ui/color-text "]")}
    "Edit the message below to encode, or arrange the cards below to decode."]
   [:div
    [:textarea
     {:class ui/textarea-class
      :rows 2
      :placeholder "Type a message to encode…"
      :value @*text
      :on-change (fn [e]
                   (let [t (-> (.. e -target -value) codec/sanitize)
                         p (codec/encode t)]
                     (reset! *text t)
                     (reset! *perm p)
                     (reset! *deck-str (h/perm->str p))))}]
    [:div {:class (str "flex text-[" ui/color-text-accent "] tracking-wider")}
     "Character Set: A–Z, a–z, 0–9, punctuation (ASCII)"
     [:div {:class "grow"}]
     [char-counter]]]

   [ui/arrow]

   [:div
    [h/in-view {:*string *deck-str
                :*deck *perm
                :on-perm-change! (fn [p]
                                   (reset! *text (codec/decode p)))}]
    [:div {:class "overflow-x-auto"}
     [ui/rearrangeable-deck *perm
      (fn []
        (let [p @*perm]
          (reset! *deck-str (h/perm->str p))
          (reset! *text (codec/decode p))))]]]])
