(ns deploy
  (:require [babashka.http-client :as http-client]
            [babashka.process :refer [shell]]
            [cheshire.core :as json]
            [clojure.java.io :as io]
            [clojure.set :as set]
            [clojure.string :as str]
            [taoensso.timbre :as log]))

(def site-id (delay (System/getenv "NETLIFY_SITE_ID")))
(def token (delay (System/getenv "NETLIFY_TOKEN")))

(defn path-hash [path]
  (-> (shell {:out :string} "openssl" "dgst" "-sha1" path)
      :out
      (str/split #"=")
      last
      str/trim))

(defn local-path [rel-path] (str "site/" rel-path))
(defn netlify-path [rel-path] (str "/" rel-path))

(defn site-path-hashes []
  (into {} (map (juxt identity (comp path-hash local-path))) ["index.html"]))

(defn- netlify-request
  [{:keys [json-body urn method stream-body] :or {method "POST"}}]
  (let [uri (str "https://api.netlify.com/api/v1/" urn)
        opts (cond-> {:uri (str "https://api.netlify.com/api/v1/" urn)
                      :method method
                      :throw false
                      :headers {"Authorization" (str "Bearer " @token)}}
               json-body (-> (assoc :body (json/encode json-body))
                             (assoc-in [:headers "Content-Type"] "application/json"))
               stream-body (assoc :body stream-body))
        {:keys [body status]} (http-client/request opts)]
    (if (< 399 status)
      (log/error {:method method :uri uri :status status :body body})
      (json/parse-string body true))))

(defn deploy-file-in-path! [rel-path deploy-id]
  (log/info "Deploying" (str (str rel-path "...")))
  (netlify-request {:urn (str "deploys/" deploy-id "/files" (netlify-path rel-path))
                    :method "PUT"
                    :stream-body (io/input-stream (local-path rel-path))}))

(defn deploy-site! []
  (let [rel-paths-hashes (site-path-hashes)
        hashes-rel-paths (set/map-invert rel-paths-hashes)
        {deploy-id :id changed-hashes :required}
        (netlify-request
         {:json-body {:files (update-keys rel-paths-hashes netlify-path)}
          :urn (str "sites/" @site-id "/deploys")})]
    (if-let [changed-rel-paths (seq (map hashes-rel-paths changed-hashes))]
      (run! #(deploy-file-in-path! % deploy-id) changed-rel-paths)
      (log/info "No changes to deploy."))))

(deploy-site!)
