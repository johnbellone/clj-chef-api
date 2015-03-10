(ns chef-api.core
  (:require [cheshire.core :as json]
            [org.httpkit.client :as http]
            [clj-time.core :as time]
            [clj-crypto.core :as crypto]
            [clojure.data.codec.base64 :as b64]
            [environ.core :refer [env]]
            [pandect.algo.sha1 :as algo]))

(def *default-headers*
  {:Content-Type "application/json"
   :Accept "application/json"
   :X-Chef-Version "12.1.0"
   :X-Ops-Sign "algorithm=sha1;version=1.0;"})

(def ^:dynamic chef-server-url (env :chef-server-url))

(defn make-authorization-headers
  [method hashed-path request-header]
  (let [content-hash (:X-Content-Hash request-header)
        timestamp (:X-Ops-Timestamp request-header)
        userid (:X-Ops-UserId request-header)
        canonical-header (str "Method:" method \newline
                              "Hashed Path:" hashed-path \newline
      xo                        "X-Ops-Content-Hash:" content-hash \newline
                              "X-Ops-Timestamp" timestamp \newline
                              "X-Ops-UserId:" userid)]
    (when-let [bytes (b64/encode (algo/sha1-bytes canonical-header))]
      )))

(defn make-authentication-headers [client-name client-key & options]
  (let [headers (or (:headers options) *default-headers*)
        signing-key (slurp client-key)
        method (:method options)
        host (:host options)
        body (:body options)]
    (when host (headers :Host host))
    (headers :X-Chef-UserId client-name)
    (headers :X-Ops-Timestamp time/now)
    (headers :X-Content-Hash (b64/encode (algo/sha1-bytes body)))
    (headers :X-Authorization)))

(defmacro with-chef-server [new-chef-server & body]
  `(binding [chef-server-url ~new-chef-server]
     ~@body))
