
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
    (replace-first ".json" "")
    (replace-first ".edn" "")))

(defn- replace-filename [source-path]
  (-> source-path
    (replace-first "cirru-" "")
    (replace-extension)))

(defn- is-cirru [f]
  (some? (re-matches #".*\.cirru" f)))

(defn- is-json [f]
  (some? (re-matches #".*\.json" f)))

(defn- is-edn [f]
  (some? (re-matches #".*\.edn" f)))

(defn- is-source [f]
  (or
    (is-cirru (.getName f))
    (is-json (.getName f))
    (is-edn (.getName f))))

(defn- compile-code [code]
  (sepal/make-code (parser/pare code "")))

(defn- compile-json-code [code]
  (sepal/make-code (json/read-str code)))

(defn- compile-edn-code [code]
  (sepal/make-code (read-string code)))

(defn- compile-source [filename source]
  (cond
    (is-cirru filename) (compile-code source)
    (is-json filename) (compile-json-code source)
    :else (compile-edn-code source)))

(defn- shorten-filename [f]
  (clojure.string/replace (.getAbsolutePath f) (cwd) ""))

(defn- compile-file [f]
  (println (str "Compiling: " f))
  (let
    [
     filename (shorten-filename f)
     source (slurp filename)
     result (compile-source filename source)]
    (with-open [wrtr (io/writer (replace-extension filename))]
      (.write wrtr result))
    (io/make-parents (replace-filename filename))
    (.renameTo
      (File. (replace-extension filename))
      (File. (replace-filename filename)))))

(defn- compile-all [paths]
  (println "Start compiling files in:" paths)
  (->> paths
    (map (fn [path]
      (file-seq (io/file path))))
    (apply concat)
    (filter is-source)
    (map (fn [path] (compile-file path)))
    (doall)))

(defn- listen-file [event]
  (if
    (is-source (:file event))
    (compile-file (:file event))))

(defn- watch-all [paths alone]
  (println "Start watching files.")
  (hawk/watch! [{:paths paths
                 :handler (fn [context event]
                            (listen-file event)
                            context)}])
  (if alone (loop []
      (Thread/sleep 300)
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

(boot/deftask transform-cirru
  "task to transform data into Clojure"
  []
  (boot/with-pre-wrap fileset
    (let [json-files (boot/by-re [#"\.json$" #"\.edn$" #"\.cirru$"]
            (boot/input-files fileset))
          tmp (boot/tmp-dir!)]
      (doseq [json-file json-files]
        (let [json-name (boot/tmp-path json-file)
              new-target (io/file tmp (replace-extension json-name))]
          (io/make-parents new-target)
          (spit new-target
            (compile-source json-name (slurp (boot/tmp-file json-file))))))
      (-> fileset
        (boot/add-resource tmp)
        (boot/rm json-files)
        (boot/commit!)))))