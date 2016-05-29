
(set-env!
  :resource-paths #{"src/"}
  :source-paths #{"cirru-src"}

  :dependencies '[[org.clojure/clojure  "1.8.0"   :scope "provided"]
                  [boot/core            "2.3.0"   :scope "provided"]
                  [org.clojure/data.json "0.2.6"]
                  [cirru/sepal          "0.0.11"]
                  [cirru/parser         "0.0.3"]
                  [hawk                 "0.2.5"]])

(require '[cirru-sepal.core :refer :all])

(def +version+ "0.1.6")

(task-options!
  pom {:project     'cirru/boot-cirru-sepal
       :version     +version+
       :description "Boot task to compile Cirru files into Clojure"
       :url         "https://github.com/Cirru/boot-cirru-sepal"
       :scm         {:url "https://github.com/Cirru/boot-cirru-sepal"}
       :license     {"MIT" "http://opensource.org/licenses/mit-license.php"}})

(set-env! :repositories #(conj % ["clojars" {:url "https://clojars.org/repo/"}]))

(deftask just-compile []
  (cirru-sepal :paths ["cirru-demo"]))

(deftask watch-compile []
  (cirru-sepal :paths ["cirru-demo"] :watch true :alone true))

(deftask build []
  (comp
   (pom)
   (jar)
   (install)
   (target)))

(deftask deploy []
  (comp
   (build)
   (push :repo "clojars" :gpg-sign (not (.endsWith +version+ "-SNAPSHOT")))))

(deftask demo-transform []
  (comp
    (transform-cirru)
    (target)))
