(ns deck-db.ui.app
  (:require
   [reagent.core :as r]
   [deck-db.ui.common :as ui]
   [deck-db.tabs.decode :as tab-decode]
   [deck-db.tabs.decode-by-hand :as tab-decode-by-hand]
   [deck-db.tabs.encode :as tab-encode]
   [deck-db.tabs.encode-by-hand :as tab-encode-by-hand]))

(defonce active-tab-id (r/atom :tab/encode))

(def tabs
  (->> [{:tab/id :tab/encode
        :tab/label "Encode"
        :tab/view-fn tab-encode/tab}
        {:tab/id :tab/encode-by-hand
         :tab/label "Encode by Hand"
         :tab/view-fn tab-encode-by-hand/tab}
        {:tab/id :tab/decode
         :tab/label "Decode"
         :tab/view-fn tab-decode/tab}
        {:tab/id :tab/decode-by-hand
         :tab/label "Decode by Hand"
         :tab/view-fn tab-decode-by-hand/tab}]
       (map (fn [t]
              [(:tab/id t) t]))
       (into {})))

(defn tab-nav []
  [:nav
   {:class (str "flex border-b-2 border-[" ui/color-border "] mb-4")}
   (doall
    (for [[tab-id tab] tabs]
      ^{:key tab-id}
      [:button
       {:class (str "bg-transparent border-0 border-b-3 border-solid mb-[-2px] "
                    "py-3 px-5 text-2xl tracking-wider grow "
                    "cursor-pointer transition-colors duration-150 whitespace-nowrap "
                    (if (= @active-tab-id tab-id)
                      (str "text-[" ui/color-text "] [border-bottom-color:" ui/color-highlight "]")
                      (str "text-[" ui/color-bg-dark "] border-transparent hover:text-[" ui/color-text-secondary "]")))
        :on-click (fn [_]
                    (reset! active-tab-id tab-id))}
       (:tab/label tab)]))])

(defn header []
  [:header
   {:class "text-center p-10"}
   [:div
    [:div {:class "relative"}
     [:img {:src "cards/card_back.png"
            :class "absolute top-0 left-0"}]
     [:img {:src "cards/card_back.png"
            :class "absolute top-2 left-2"}]
     [:img {:src "cards/card_joker_red.png"
            :class "absolute top-4 left-4"}]]
   [:h1
    {:class (str "text-5xl tracking-wider text-[" ui/color-text "]")}
    "Deck DB"]]
   [:p
    {:class (str "text-[" ui/color-text-accent "] tracking-wider")}
    "store a message in a deck of cards"]])

(defn app []
  [:div
   {:class (str "min-h-screen bg-[" ui/color-bg-app "]")}
   [:div
    {:class "mx-auto"
     :style {:max-width ui/width}}
    [header]
    [tab-nav]
    [(:tab/view-fn (tabs @active-tab-id))]]])


