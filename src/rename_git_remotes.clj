(ns rename-git-remotes
  (:require [clojure.string :as s]
            [clojure.java.io :as io]
            [clojure.pprint :refer [print-table]]))

(defn get-files
  [path]
  (let [file (io/file path)]
    (println "Updating git remotes in" (.getAbsolutePath file))
    (file-seq file)))

(defn git-config?
  [file]
  (s/ends-with? (str file) ".git/config"))

(defn replace-remote
  [from-remote to-remote line]
  (s/replace line (re-pattern from-remote) to-remote))

(defn read-config
  [file]
  (with-open [r (io/reader file)]
    (into [] (line-seq r))))

(defn write-config
  [file lines]
  (with-open [w (io/writer file)]
    (doseq [line lines]
      (.write w (str line "\n")))))

(defn update-remote
  [from-remote to-remote file]
  (let [lines (read-config file)]
    (->> lines
         (map #(replace-remote from-remote to-remote %))
         (write-config file))
    {:file (str file) :lines (count lines)}))

(defn display-help
  []
  (println "")
  (println "Usage: rename-git-remotes from-remote to-remote projects-path")
  (println "")
  (println "Updates remotes in each .git/config within projects-path")
  (println "replacing from-remote with to-remote.")
  (println "")
  (println ""))

(defn -main
  [& [from to path]]
  (when (or (= from "--help")
            (= from "-h"))
    (display-help)
    (System/exit 0))
  (when (some #(or (not %) (empty? %)) [from to path])
    (println "Error: Invalid arguments.")
    (display-help)
    (System/exit 1))
  (print-table
   [:file :lines]
   (into [] (comp (filter git-config?)
                  (take 1)
                  (map #(update-remote from to %)))
            (get-files path))))
