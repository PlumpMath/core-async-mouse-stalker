(set-env! :dependencies '[[adzerk/boot-cljs "1.7.170-3"]
                          [org.clojure/clojure "1.7.0"]
                          [org.clojure/core.async "0.2.374"]
                          [pandeiro/boot-http "0.7.1-SNAPSHOT"]
                          [prismatic/dommy "1.1.0"]]
          :resource-paths #{"assets"}
          :source-paths #{"src"})

(require
  '[adzerk.boot-cljs :refer :all]
  '[pandeiro.boot-http :refer [serve]])

(deftask develop
  []
  (comp
    (serve :dir "target" :port 3000)
    (watch)
    (speak)
    (cljs :optimizations :none)))

(deftask release
  []
  (cljs :optimizations :simple))
