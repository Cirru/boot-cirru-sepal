
(ns cirru-sepal.core
  (:require [cirru.sepal :as sepal]
            [cirru.parser.core :as parser]
            [boot.core :as boot]
            [clojure.java.io :as io]
            [clojure.string :refer (replace-first)]
            [clojure.data.json :as json]
            [hawk.core :as hawk])
  (:import (java.io File)))

(defn- cwd [] (str (System/getenv "PWD") "/"))

(defn- replace-extension [source-path]
  (-> source-path
    (replace-first ".cirru" "")
    (replace-first ".json" "")))

(defn- replace-filename [source-path]
  (-> source-path
    (replace-first "cirru-" "")
    (replace-extension)))

(defn- is-cirru [f]
  (some? (re-matches #".*\.cirru" f)))

(defn- is-json [f]
  (some? (re-matches #".*\.json" f)))

(defn- compile-code [code]
  (sepal/make-code (parser/pare code "")))

(defn- compile-json-code [code]
  (sepal/make-code (json/read-str code)))

(defn- compile-file [filename]
  (println (str "Compiling: " filename))
  (let
    [source (slurp filename)
     result (if (is-cirru filename)
                (compile-code source)
                (compile-json-code source))]
    (with-open [wrtr (io/writer (replace-extension filename))]
      (.write wrtr result))
    (io/make-parents (replace-filename filename))
    (.renameTo
      (File. (replace-extension filename))
      (File. (replace-filename filename)))))

(defn- compile-all [paths]
  (println "Start compiling files.")
  (doall (map
    (fn [path] (compile-file (.getName path)))
    (filter
      (fn [x] (or (is-cirru (.getName x)) (is-json (.getName x))))
      (apply concat
        (map (fn [path] (file-seq (io/file path))) paths))))))

(defn- listen-file [event]
  (if
    (or
      (is-cirru (.getName (:file event)))
      (is-json (.getName (:file event))))
    (let
      [ filename (.getAbsolutePath (:file event))
        relativePath (clojure.string/replace filename (cwd) "")]
      (compile-file relativePath))))

(defn- watch-all [paths alone]
  (println "Start watching files.")
  (hawk/watch! [{:paths paths
                 :handler (fn [context event]
                            (listen-file event)
                            context)}])
  (if alone (loop []
      (Thread/sleep 400)
      (recur))))

(boot/deftask cirru-sepal
  "task to compile Cirru into Clojure"
  [p paths VAL [str]  "Paths to compile"
   w watch      bool   "Enable watch mode"
   a alone      bool   "Run a standalone task"]
  (let [watching (if (some? watch) watch false)]
    (fn [next-task]
      (fn [fileset]
        (let []
          (if watching
            (watch-all paths (if (some? alone) alone false))
            (compile-all paths))
          (next-task fileset))))))
