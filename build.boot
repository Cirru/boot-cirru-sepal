
(set-env!
  :source-paths #{"src"}

  :dependencies '[[org.clojure/clojure  "1.8.0"   :scope "provided"]
                  [boot/core            "2.3.0"   :scope "provided"]
                  [org.clojure/data.json "0.2.6"]
                  [cirru/sepal          "0.0.11"]
                  [cirru/parser         "0.0.3"]])

(require '[cirru-sepal.core :refer :all])

(def +version+ "0.1.10")

(task-options!
  pom {:project     'cirru/boot-cirru-sepal
       :version     +version+
       :description "Boot task to compile Cirru files into Clojure"
       :url         "https://github.com/Cirru/boot-cirru-sepal"
       :scm         {:url "https://github.com/Cirru/boot-cirru-sepal"}
       :license     {"MIT" "http://opensource.org/licenses/mit-license.php"}})

(set-env! :repositories #(conj % ["clojars" {:url "https://clojars.org/repo/"}]))

(deftask build []
  (set-env!
    :resource-paths #{"src/"})
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
  (set-env!
    :source-paths #{"cirru/"})
  (comp
    (transform-cirru)
    (target)))

(deftask watch-transform []
  (set-env!
    :source-paths #{"cirru/"})
  (comp
    (watch)
    (transform-cirru)
    (target)))
