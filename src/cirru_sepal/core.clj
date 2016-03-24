
(ns cirru-sepal.core
  (:require [cirru.sepal :as sepal]
            [cirru.parser.core :as parser]
            [boot.core :as boot]
            [clojure.java.io :as io]
            [clojure.string :refer (replace-first)]
            [hawk.core :as hawk])
  (:import (java.io File)))

(defn- cwd [] (str (System/getenv "PWD") "/"))

(defn- replace-extension [source-path]
  (replace-first source-path ".cirru" ""))

(defn- replace-filename [source-path]
  (-> source-path
    (replace-first "cirru-" "")
    (replace-first ".cirru" "")))

(defn- compile-code [code]
  (sepal/make-code (parser/pare code "")))

(defn- compile-file [filename]
  (println (str "Compiling: " filename))
  (let
    [result (compile-code (slurp filename))]
    (with-open [wrtr (io/writer (replace-extension filename))]
      (.write wrtr result))
    (io/make-parents (replace-filename filename))
    (.renameTo
      (File. (replace-extension filename))
      (File. (replace-filename filename)))))

(defn- is-cirru [f]
  (some? (re-matches #".*\.cirru" (.getName f))))

(defn- compile-all [paths]
  (println "Start compiling files.")
  (doall (map
    (fn [path] (compile-file path))
    (filter is-cirru
      (apply concat
        (map (fn [path] (file-seq (io/file path))) paths))))))

(defn- listen-file [event]
  (if (is-cirru (:file event))
    (let
      [ filename (.getAbsolutePath (:file event))
        relativePath (clojure.string/replace filename (cwd) "")]
      (compile-file relativePath))))

(defn- watch-all [paths]
  (println "Start watching files.")
  (hawk/watch! [{:paths paths
                 :handler (fn [context event]
                            (listen-file event)
                            context)}])
  (loop []
    (Thread/sleep 400)
    (recur)))

(boot/deftask cirru-sepal
  "task to compile Cirru into Clojure"
  [p paths VAL [str]  "Paths to compile"
   w watch bool   "Enable watch mode"]
  (let []
    (fn [next-task]
      (fn [fileset]
        (let []
          (if watch
            (watch-all paths)
            (compile-all paths))
          (next-task fileset))))))
