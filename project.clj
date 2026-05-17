(defproject deck-db "0.1.0-SNAPSHOT"
  :dependencies [[org.clojure/clojure "1.12.5"]
                 [org.clojure/clojurescript "1.12.134"]
                 [reagent "2.0.1"]
                 [cljsjs/react "18.3.1-1"]
                 [cljsjs/react-dom "18.3.1-1"]]

  :plugins [[lein-figwheel "0.5.20"]
            [lein-cljsbuild "1.1.8"]]

  :source-paths ["src"]

  :cljsbuild
  {:builds
   [{:id           "dev"
     :source-paths ["src"]
     :figwheel     {:on-jsload "deck-db.core/on-js-reload"}
     :compiler     {:main                 deck-db.core
                    :asset-path           "js/out"
                    :output-to            "resources/public/js/main.js"
                    :output-dir           "resources/public/js/out"
                    :source-map-timestamp true}}]}

  :figwheel {:server-port 3449})
