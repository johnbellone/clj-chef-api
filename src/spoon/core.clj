(ns spoon.core
  (:gen-class)
  (:use clj-logging-config.log4j)
  (:require [clojure.string :as str]
            [clojure.tools.logging :as log]
            [cheshire.core :as json]
            [org.httpkit.client :as http]
            [clj-time.core :as time]
            [environ.core :refer [env]]
            [spoon.util.crypto :as crypto]))

(def default-headers
  "Common headers that are the same with each request."
  {:Content-Type "application/json"
   :Accept "application/json"
   :X-Chef-Version "12.2.0"
   :X-Ops-Sign "algorithm=sha1;version=1.0;"})

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
            [(keyword (str "X-Ops-Authorization-" (inc n)))
             (str/join s)])]
    (into {} (map-indexed header-n (partition-all 59 token)))))

(defn- make-authorization-headers
  "Create the X-Ops-Autherization-N headers by signing the canonical header
  information. Returns a map of the headers with these keys added."
  [method secret-key
   {path    :Path
    content :X-Ops-Content-Hash
    time    :X-Ops-Timestamp
    user    :X-Ops-UserId
    :or {path "/"}
    :as request-headers}]
  (let [canonical-headers
        (str "Method:" method \newline
             "Hashed Path:" (crypto/digest path) \newline
             "X-Ops-Content-Hash:" content \newline
             "X-Ops-Timestamp" time \newline
             "X-Ops-UserId:" user \newline)]
    (split-x-auth (-> canonical-headers
                      (crypto/encrypt secret-key)
                      (str/trim-newline)))))

(defn- make-request-headers
  "Create the headers necessary for creating a new request to the server api."
  [method {:keys [client-name client-key method host body] :as options}]
  (log/debug options)
  (let [signing-key (crypto/read-pem client-key)
        headers (merge default-headers
                       (:headers options)
                       {:Host host
                        :X-Chef-UserId client-name
                        :X-Ops-Timestamp (time/now)
                        :X-Content-Hash (crypto/digest body)})]
    (merge headers
           (make-authorization-headers method signing-key headers))))

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
  ([endpoint] (request :get endpoint))
  ([method endpoint] (request method endpoint {}))
  ([method endpoint {:keys [data query body client-name client-key] :as options}]
   (set-logger!)
   (log/debug method endpoint options)
   (let [method (str/upper-case method)
         url (clojure.java.io/as-url (str "https://" *chef-server-url* "/" *chef-organization*))
         client-name (or client-name (env :chef-client-name))
         client-key (or client-key (env :chef-client-key))
         headers (make-request-headers method (merge options {:client-name client-name
                                                              :client-key client-key}))]
     (log/debug options headers)
     @(http/request {:url url
                     :method method
                     :headers headers
                     :query-params (or query nil)
                     :body (or body nil)}))))
