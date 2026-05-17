(ns deck-db.core
  (:require
   [reagent.core :as r]
   [reagent.dom :as rdom]
   [deck-db.cards :as cards]
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
   {:class "flex border-b-2 border-[#333] mb-4"}
   (doall
    (for [[tab-id tab] tabs]
      ^{:key tab-id}
      [:button
       {:class (str "bg-transparent border-0 border-b-3 border-solid mb-[-2px] "
                    "py-3 px-5 text-2xl tracking-wider grow "
                    "cursor-pointer transition-colors duration-150 whitespace-nowrap "
                    (if (= @active-tab-id tab-id)
                      "text-[#fff] [border-bottom-color:#c8a84b]"
                      "text-[#0d3d03] border-transparent hover:text-[#ccc]"))
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
    {:class "text-5xl tracking-wider text-[#fff]"}
    "Deck DB"]]
   [:p
    {:class "text-[#7ea974] tracking-wider"}
    "store a message in a deck of cards"]])

(defn app []
  [:div
   {:class "min-h-screen bg-[#176e03]"}
   [:div
    {:class "mx-auto"
     :style {:max-width ui/width}}
    [header]
    [tab-nav]
    [(:tab/view-fn (tabs @active-tab-id))]]])

(defn mount []
  (rdom/render [app] (.getElementById js/document "app")))

(defn on-js-reload []
  (mount))

(defn ^:export main []
  (mount))
