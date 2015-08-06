(ns spoon.core
  (:gen-class)
  (:use clj-logging-config.log4j)
  (:require [clojure.string :as str]
            [clojure.java.io :as io]
            [clojure.tools.logging :as log]
            [cheshire.core :as json]
            [org.httpkit.client :as http]
            [clj-time.core :as time]
            [clj-time.format :as time.fmt]
            [spoon.util.crypto :as crypto]))

(def default-headers
  "Common headers that are the same with each request."
  {"Content-Type" "application/json"
   "Accept" "application/json"
   "X-Chef-Version" "12.2.0"
   "X-Ops-Sign" "algorithm=sha1;version=1.0;"})

(defn split-x-auth
  "Return a map of :X-Ops-Authorization-n headers from a single long string."
  [token]
  (letfn [(header-n [n s]
            [(str "X-Ops-Authorization-" (inc n))
             (str/join s)])]
    (into {} (map-indexed header-n (partition-all 60 token)))))

(defn canonicalize-request
  "Create canonical headers for sigining"
  [{:keys [method path body timestamp client-name]}]
  (str "Method:"             (str/upper-case method) \newline
       "Hashed Path:"        (crypto/digest path) \newline
       "X-Ops-Content-Hash:" (crypto/digest body) \newline
       "X-Ops-Timestamp:"    timestamp            \newline
       "X-Ops-UserId:"       client-name))

(defn make-authorization-headers
  "Create the X-Ops-Autherization-N headers by signing the canonical header
  information. Returns a map of the headers with these keys added."
  [{:keys [client-key] :as request}]
  (split-x-auth (-> (canonicalize-request request)
                    (crypto/encrypt (crypto/read-pem client-key))
                    (str/trim-newline))))

(defn format-time
  "Helper for formatting time strings in format chef-server expects. Takes a
  clj-time object and returns an iso8601 formatted string w/o mseconds.
  Defaults to current time if no parameter is passed."
  ([] (format-time (time/now)))
  ([t]
   (time.fmt/unparse (time.fmt/formatters :date-time-no-ms) t)))

(defn make-request-headers
  "Create the headers necessary for creating a new request to the server api."
  [{:keys [chef-host body client-name timestamp] :as options}]
  (let [headers (merge default-headers
                       {"Host"               chef-host
                        "X-Ops-UserId"       client-name
                        "X-Ops-Timestamp"    timestamp
                        "X-Ops-Content-Hash" (crypto/digest body)})]
    (merge headers (make-authorization-headers options))))

(defn api-request
  "Make a request using the chef server api.

  Required parameters:
    :method      - http method for the endpoint
    :path        - endpoint's url path
    :chef-host   - hostname of the node running the chef-server
    :client-name - client name string for the connecting api user
    :client-key  - path to pem file containing the clients private key

  Will return the parsed json data upon success, otherwise raises an exception
  with any http errors."
  [{:keys [method path chef-host body query timestamp]
    :or {body "", query "", timestamp (format-time)}
    :as request}]
  (let [path (if (and (not= path "/") (.startsWith path "/")) (.substring path 1) path)
        headers (make-request-headers
                  (assoc request
                         :method (name method)
                         :body body
                         :timestamp timestamp))

        {:keys [status body] :as result}
        @(http/request
           {:url (format "https://%s/%s" chef-host path)
            :method method
            :insecure? true
            :headers (log/spy :debug headers)
            :query-params query
            :body body})]
    (if (= status 200)
      (-> result (:body) (json/parse-string true))
      (throw (ex-info "Chef API error" result)))))

(defn client-info
  "Create options map w/ api client required information. Take the chef-host
  url as a string, the name of the chef client, and a path to the pem file
  containing the client's private key.
  
  Pass the returned map as the options to the api functions."
  [c n k]
  {:chef-host c
   :client-name n
   :client-key k})

