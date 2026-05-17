(ns deck-db.core
  (:require
   [reagent.core :as r]
   [reagent.dom :as rdom]))

(def active-tab (r/atom :tab/encode))

(def tabs
  [[:tab/encode          "Encode"]
   [:tab/encode-by-hand  "Encode by Hand"]
   [:tab/decode          "Decode"]
   [:tab/decode-by-hand  "Decode by Hand"]])

(defn tab-btn [[id label]]
  (let [active? (= @active-tab id)]
    [:button
     {:class    (str "bg-transparent border-0 border-b-2 border-solid -mb-px "
                     "py-[0.65rem] px-[1.3rem] font-serif text-[0.95rem] "
                     "cursor-pointer transition-colors duration-150 whitespace-nowrap "
                     (if active?
                       "text-[#f0e8d0] [border-bottom-color:#c8a84b]"
                       "text-[#888] border-transparent hover:text-[#ccc]"))
      :on-click (fn [_] (reset! active-tab id))}
     label]))

(defn tab-nav []
  [:nav
   {:class "flex border-b-2 border-[#333] mb-8"}
   (for [tab tabs]
     ^{:key (first tab)} [tab-btn tab])])

(defn panel [id content]
  [:div
   {:class (if (= @active-tab id) "block" "hidden")}
   [:div
    {:class "bg-[#242424] border border-[#333] rounded-md p-8 text-[#999] italic text-center min-h-[200px] flex items-center justify-center"}
    content]])

(defn app []
  [:div
   {:class "max-w-[760px] mx-auto"}
   [:header
    {:class "text-center mb-10"}
    [:h1
     {:class "text-[1.9rem] tracking-[0.04em] text-[#f0e8d0]"}
     "Deck DB"]
    [:p
     {:class "mt-[0.4rem] text-[0.9rem] text-[#888] italic"}
     "store a message in a deck of cards"]]
   [tab-nav]
   [panel :tab/encode          "Encode panel — coming soon"]
   [panel :tab/encode-by-hand  "Encode by Hand panel — coming soon"]
   [panel :tab/decode          "Decode panel — coming soon"]
   [panel :tab/decode-by-hand  "Decode by Hand panel — coming soon"]])

(defn mount []
  (rdom/render [app] (.getElementById js/document "app")))

(defn on-js-reload []
  (mount))

(defn ^:export main []
  (mount))
