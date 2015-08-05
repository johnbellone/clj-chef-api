(ns spoon.core
  (:gen-class)
  (:use clj-logging-config.log4j)
  (:require [clojure.string :as str]
            [clojure.tools.logging :as log]
            [cheshire.core :as json]
            [org.httpkit.client :as http]
            [clj-time.core :as time]
            [clj-time.format :as time.fmt]
            [environ.core :refer [env]]
            [spoon.util.crypto :as crypto]))

(def default-headers
  "Common headers that are the same with each request."
  {"Content-Type" "application/json"
   "Accept" "application/json"
   "X-Chef-Version" "12.2.0"
   "X-Ops-Sign" "algorithm=sha1;version=1.0;"})

(def ^:dynamic *chef-server-url*
  "URL to the Chef Server API."
  (or (env :chef-server-url) "manage.chef.io"))

(def ^:dynamic *chef-organization*
  "Name of the Chef Organization"
  (or (env :chef-organization) "bloomberg"))

(defn- split-x-auth
  "Return a map of :X-Ops-Authorization-n headers from a single hmac token
  string."
  [token]
  (letfn [(header-n [n s]
            [(str "X-Ops-Authorization-" (inc n))
             (str/join s)])]
    (into {} (map-indexed header-n (partition-all 60 token)))))

(defn canonicalize-request
  "Create canonical headers for sigining"
  [{:keys [http-method path body timestamp user-id]}]
  (str "Method:" (str/upper-case http-method) \newline
       "Hashed Path:" (crypto/digest path) \newline
       "X-Ops-Content-Hash:" (crypto/digest body) \newline
       "X-Ops-Timestamp:" timestamp \newline
       "X-Ops-UserId:" user-id))

(defn- make-authorization-headers
  "Create the X-Ops-Autherization-N headers by signing the canonical header
  information. Returns a map of the headers with these keys added."
  [verb body secret-key
   {path "Path", time "X-Ops-Timestamp", user "X-Ops-UserId"
    :or {path "/"} :as request-headers}]
  (let [canonical-headers (canonicalize-request
                            {:http-method verb
                             :path path
                             :body body
                             :timestamp time
                             :user-id user})]
    (split-x-auth (-> canonical-headers
                      (crypto/encrypt secret-key)
                      (str/trim-newline)))))

(defn format-time
  "Helper for formatting time strings in format chef-server expects. Takes a
  clj-time object and returns an iso8601 formatted string w/o mseconds.
  Defaults to current time if no parameter is passed."
  ([] (format-time (time/now)))
  ([t]
   (time.fmt/unparse (time.fmt/formatters :date-time-no-ms) t)))

(defn- make-request-headers
  "Create the headers necessary for creating a new request to the server api."
  [verb path {:keys [client-name client-key host body timestamp] :as options}]
  (let [signing-key (crypto/read-pem client-key)
        body (or body "")
        headers (merge default-headers
                       (:headers options)
                       {"Host" host
                        "Path" path
                        "X-Ops-UserId" client-name
                        "X-Ops-Timestamp" (or timestamp (format-time))
                        "X-Ops-Content-Hash" (crypto/digest body)})]
    (merge headers
           (make-authorization-headers verb body signing-key headers))))

(defn- inspect-headers
  "Print out the headers from the provided map. Formatted similar to how the
  would appear in an actual http request."
  [m]
  (let [headers (sort-by first m)
        print-header (fn [k v]
                       (println (format "%s: %s" (name k) v)))]
    (dorun (for [[k v] headers] (print-header k v)))))

(defmacro with-chef-server [url & body]
  `(binding [*chef-server-url* ~url]
     ~@body))

(defmacro with-chef-organization [org & body]
  `(binding [*chef-organization* ~org]
     ~@body))

(defn request
  ([endpoint] (request "get" endpoint))
  ([verb endpoint] (request verb endpoint {}))
  ([verb endpoint {:keys [data query body client-name client-key] :as options}]
   (set-logger!)
   (log/debug verb endpoint options)
   (let [verb (str/lower-case (name verb))
         host *chef-server-url*
         url (clojure.java.io/as-url (str "https://" host endpoint))
         client-name (or client-name (env :chef-client-name))
         client-key (or client-key (env :chef-client-key))
         headers (make-request-headers verb endpoint (merge options {:client-name client-name
                                                                     :client-key client-key
                                                                     :host host}))]
     (log/debug options headers)
     (-> @(http/request {:url (str url)
                         :method (keyword verb)
                         :insecure? true
                         :headers headers
                         :query-params (or query {})
                         :body (or body "")})
         :body
         (json/parse-string true)))))
