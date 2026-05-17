(defproject codeck "0.1.0-SNAPSHOT"
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
     :figwheel     {:on-jsload "codeck.core/on-js-reload"}
     :compiler     {:main                 codeck.core
                    :asset-path           "js/out"
                    :output-to            "resources/public/js/main.js"
                    :output-dir           "resources/public/js/out"
                    :source-map-timestamp true}}
    {:id           "prod"
     :source-paths ["src"]
     :compiler     {:main          codeck.core
                    :output-to     "resources/public/js/main.js"
                    :optimizations :advanced
                    :pretty-print  false}}]}

  :figwheel {:server-port 3449})
