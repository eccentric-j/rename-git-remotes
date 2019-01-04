(ns rename-git-remotes.cli
  (:require [clojure.string :as s]
            [clojure.java.io :as io]
            [clojure.pprint :refer [print-table]])
  (:import java.io.File
           java.time.LocalTime))

(defn get-files
  "Lists files path directory.
  Takes path string where .git/config files can be found like ~/Projects.
  Returns seq of files."
  [path]
  (let [file (io/file path)]
    (println "Updating git remotes in" (.getAbsolutePath file))
    (file-seq file)))

(defn git-config?
  "Is a file a .git/config file?
  Takes a java.io.File
  Returns true if file path ends with .git/config"
  [^File file]
  (s/ends-with? (str file) ".git/config"))

(defn ->remote-update
  "Creates a map to describe a remote update.
  Takes a from-remote string and a to-remote string along with a .git/config
  file to update.
  Returns map of {:from regexp :to str :src File :dest File :count int}."
  [from-remote to-remote ^File src-config-file]
  {:from (re-pattern from-remote)
   :to to-remote
   :src src-config-file
   :dest (File/createTempFile "config" "")
   :count 0})

(defn replace-remote
  "Replaces the from remote with the to remote in a .git/config line
  Takes a remote-update map and the line of config to update.
  Returns updated line of config str."
  [{:keys [from to]} line]
  (str (s/replace line from to) "\n"))

(defn update-remotes
  "Reads a .git/config file and writes updated config to temp file. Avoids
  holding .git/config file contents in memory by writing to temp file.
  Takes a remote-update map.
  Returns updated remote-update map"
  [{:keys [src dest] :as update}]
  (let [count (atom 0)]
    (with-open [r (io/reader src)
                w (io/writer dest)]
      (doseq [line (line-seq r)]
        (.write w (replace-remote update line))
        (swap! count inc)))
    (assoc update :count @count)))

(defn replace-config-file
  "Replaces .git/config with updated temp file.
  Takes a remote-update map.
  Returns remote-update map."
  [{:keys [src dest] :as update}]
  ;; replaces the original src file with the updated dest file
  (.renameTo dest src)
  update)

(defn format-for-display
  "Return an object we can display in output table to describe the update.
  Takes a remote-update map.
  Returns a new map to summarize the update."
  [{:keys [src count]}]
  {:file (str src) :count count :updated (LocalTime/now)})

(defn update-config-files
  "Updates each .git/config file and returns a summary map for final output.
  Takes a from-remote str and a to-remote str and a io.File.
  Returns a map that summarizes the config update."
  [from-remote to-remote file]
  (let [pending-update (->remote-update from-remote to-remote file)]
    (-> pending-update
        (update-remotes)
        (replace-config-file)
        (format-for-display))))

(defn display-help
  "Display cli usage info to the user."
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
  (time (do
          (print-table
            [:file :count :updated]
            (into [] (comp (filter git-config?)
                           (map #(update-config-files from to %)))
                     (get-files path)))
          (println "")))
  (println ""))
