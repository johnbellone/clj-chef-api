(ns chef-api-client.core
  (:require [cheshire.core :as json]
            [org.httpkit.client :as http]
            [clj-time.core :as time]
            [clj-crypto.core :as crypto]
            [clojure.data.codec.base64 :as b64]
            [environ.core :refer [env]]
            [pandect.algo.sha1 :as algo]))

(def ^{:const true} *default-headers*
  {:Content-Type "application/json"
   :Accept "application/json"
   :X-Chef-Version "12.2.0"
   :X-Ops-Sign "algorithm=sha1;version=1.0;"})

(def ^:dynamic *chef-server-url* (env :chef-server-url))

(defn string->b64-sha1
  "Return a base64 encoded sha1 bytes of value."
  [value]
  (b64/encode (algo/sha1-bytes value)))

(defn make-authorization-headers
  [method request-header]
  (let [path (:Path request-header)
        content-hash (:X-Content-Hash request-header)
        timestamp (:X-Ops-Timestamp request-header)
        userid (:X-Ops-UserId request-header)
        headers (str "Method:" method \newline
                     "Hashed Path:" (string->b64-sha1 path) \newline
                     "X-Ops-Content-Hash:" content-hash \newline
                     "X-Ops-Timestamp" timestamp \newline
                     "X-Ops-UserId:" userid \newline)
        bytes (string->b64-sha1 headers)]
    (map (fn [s n]
           (assoc {} (symbol (str ":X-Ops-Authorization-" (inc n))) s))
         (partition-all 59 bytes) (range))))

(defn make-request-headers [client-name client-key & options]
  (let [headers (or (:headers options) *default-headers*)
        signing-key (slurp client-key)
        method (:method options)
        host (:host options)
        body (:body options)]
    (when host (headers :Host host))
    (headers :X-Chef-UserId client-name)
    (headers :X-Ops-Timestamp time/now)
    (headers :X-Content-Hash (string->b64-sha1 body))
    (reduce merge headers (make-authorization-headers method headers))))

(defn make-request [method endpoint]
  (http/request {:url (str *chef-server-url* endpoint)
                 :method method
                 :keepalive 1000}))

(defmacro with-chef-server [new-chef-server & body]
  `(binding [chef-server-url ~new-chef-server]
     ~@body))
