(ns render
  (:require [babashka.fs :as fs]
            [hiccup2.core :as h]
            [markdown.core :as md]
            [taoensso.timbre :as log]))

(def header-comment "<!--
  Thanks for checking out the source!
  Here you will find all the exciting technologies of the Internet age!
-->")

(defn index []
  [:html {:lang "en"}
   [:head
    [:meta {:http-equiv "content-type" :content "text/html; charset=utf-8"}]
    [:meta {:name "description" :content "My home page on the world wide web."}]
    [:meta {:name "viewport" :content "width=device-width, initial-scale=1"}]
    [:title "Hi, my name is Kārlis."]
    [:style {:media "screen"} (h/raw (slurp "site/static/style.css"))]]
   [:body (h/raw (md/md-to-html-string (slurp "site/index.md")))]])

(defn render-index! []
  (log/info "Rendering index...")
  (fs/create-dirs "target")
  (spit "target/index.html" (str "<!DOCTYPE HTML>\n\n" header-comment "\n\n" (h/html (index)))))

(render-index!)
