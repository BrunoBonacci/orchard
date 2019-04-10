(ns orchard.classloader
  (:require
   [clojure.string :as str]
   [orchard.java :as java]
   [orchard.misc :refer [boot-fake-classpath boot-project?]]))

(defn boot-class-loader
  "Creates a class-loader that knows original source files paths in Boot project."
  []
  (let [class-path (boot-fake-classpath)
        dir-separator (System/getProperty "file.separator")
        paths (str/split class-path (re-pattern (System/getProperty "path.separator")))
        urls (map
              (fn [path]
                (let [url (if (re-find #".jar$" path)
                            (str "file:" path)
                            (str "file:" path dir-separator))]
                  (new java.net.URL url)))
              paths)
        jdk-sources (->> ["src.zip" "tools.jar"]
                         (map java/jdk-find)
                         (remove nil?))]
    (new java.net.URLClassLoader (into-array java.net.URL (concat urls jdk-sources)))))

(defn class-loader
  []
  (if (boot-project?)
    (boot-class-loader)
    (.getContextClassLoader (Thread/currentThread))))
