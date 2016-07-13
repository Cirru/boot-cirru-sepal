
(ns cirru-sepal.core
  (:require [cirru.sepal :as sepal]
            [cirru.parser.core :as parser]
            [boot.core :as boot]
            [clojure.java.io :as io]
            [clojure.string :refer (replace-first)]
            [clojure.data.json :as json])
  (:import (java.io File)))

(defn- cwd [] (str (System/getenv "PWD") "/"))

(defn- replace-extension [source-path]
  (-> source-path
    (replace-first ".cirru" "")
    (replace-first ".json" "")
    (replace-first ".ir" "")))

(defn- is-cirru [f]
  (some? (re-matches #".*\.cirru" f)))

(defn- is-json [f]
  (some? (re-matches #".*\.json" f)))

(defn- is-edn [f]
  (some? (re-matches #".*\.ir" f)))

(defn- is-source [f]
  (or
    (is-cirru (.getName f))
    (is-json (.getName f))
    (is-edn (.getName f))))

(defn- compile-code [code]
  (sepal/make-code (parser/pare code "")))

(defn- compile-json-code [code]
  (sepal/make-code (json/read-str (if (= "" code) "[]" code))))

(defn- compile-edn-code [code]
  (sepal/make-code (read-string (if (= "" code) "[]" code))))

(defn- compile-source [filename source]
  (println "compile file:" filename)
  (cond
    (is-cirru filename) (compile-code source)
    (is-json filename) (compile-json-code source)
    :else (compile-edn-code source)))

(boot/deftask transform-cirru
  "task to transform data into Clojure"
  []
  (let [last-files (atom nil)
        tmp (boot/tmp-dir!)]
    (boot/with-pre-wrap fileset
      (let [json-files
              (->> fileset
                (boot/fileset-diff @last-files)
                (boot/input-files)
                (boot/by-re [#"\.json$" #"\.ir$" #"\.cirru$"]))]
        (reset! last-files fileset)
        (doseq [json-file json-files]
          (let [json-name (boot/tmp-path json-file)
                new-target (io/file tmp (replace-extension json-name))]
            (io/make-parents new-target)
            (spit new-target
              (compile-source json-name (slurp (boot/tmp-file json-file))))))
        (-> fileset
          (boot/add-resource tmp)
          (boot/rm json-files)
          (boot/commit!))))))
