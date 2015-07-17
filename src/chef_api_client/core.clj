(ns chef-api-client.core
  (:require
    [cheshire.core :as json]
    [org.httpkit.client :as http]
    [clj-time.core :as time]
    [clj-crypto.core :as crypto]
    [clojure.data.codec.base64 :as b64]
    [environ.core :refer [env]]
    [pandect.algo.sha1 :as algo])
  (:import
    [org.bouncycastle.jce.provider BouncyCastleProvider]
    [org.bouncycastle.openssl PEMParser]
    [org.bouncycastle.crypto.util PublicKeyFactory PrivateKeyFactory]))

;;; BC Crypto

(defn init-crypto []
  (java.security.Security/addProvider (BouncyCastleProvider.)))

(defn- read-key [path]
  (-> path (java.io.FileReader.) (PEMParser.) (.readObject))) 

(defn- pem->bc-pub-key
  [path]
  (-> (read-key path) (.getPublicKeyInfo) (PublicKeyFactory/createKey))) 

(defn pem->jce-pub-key
  [path]
  (let [{:keys [exponent modulus]} (into {} (seq (bean (pem->bc-pub-key path))))
        factory (java.security.KeyFactory/getInstance "RSA")
        spec (java.security.spec.RSAPublicKeySpec. modulus exponent)]
    (.generatePublic factory spec)))

;;; Hash/Encoding utilities

(def ^{:arglists '([^bytes buffer])
       :doc "Encode base64 encoded byte-array as a UTF-8 string."
       :private true
       :nodoc true}
  b64-string
  (comp #(String. % "UTF-8") b64/encode))

(def ^{:arglists '([^String value secret-key])
       :doc "Return a base64 encoded hmac-sha1 token from value. Signed with
            secret-key."
       :private true
       :nodoc true}
  hmac-sha
  (comp b64-string algo/sha1-hmac-bytes))

(def ^{:arglists '([^String value])
       :doc "Return a base64 encoded string SHA1 digest from value."
       :private true
       :nodoc true}
  digest
  (comp b64-string algo/sha1-bytes))

;;; Creating requests

(def default-headers
  {:Content-Type "application/json"
   :Accept "application/json"
   :X-Chef-Version "12.2.0"
   :X-Ops-Sign "algorithm=sha1;version=1.0;"})

(defn split-x-auth
  "Return a map of :X-Ops-Authorization-n headers from a single hmac token
  string."
  [token]
  (letfn [(header-n [n s]
            [(keyword (str "X-Ops-Authorization-" n))
             (apply str s)])]
    (into {} (map-indexed header-n (partition-all 59 token)))))

(defn make-authorization-headers
  [method secret-key
   {:keys [Path X-Ops-Content-Hash X-Ops-Timestamp X-Ops-UserId]
    :or {Path "/"}
    :as request-headers}]
  (let [canonical-headers
        (str "Method:" method \newline
             "Hashed Path:" (digest Path) \newline
             "X-Ops-Content-Hash:" X-Ops-Content-Hash \newline
             "X-Ops-Timestamp" X-Ops-Timestamp \newline
             "X-Ops-UserId:" X-Ops-UserId \newline)]
    (split-x-auth (hmac-sha canonical-headers secret-key))))

;; (defn make-request-headers
;;   "Return"
;;   [client-name client-key & options]
;;   (let [headers (or (:headers options) *default-headers*)
;;         signing-key (slurp client-key)
;;         method (clojure.string/upper-case (:method options))
;;         host (:host options)
;;         body (:body options)]
;;     (when host (headers :Host host))
;;     (headers :X-Chef-UserId client-name)
;;     (headers :X-Ops-Timestamp time/now)
;;     (headers :X-Content-Hash (digest body))
;;     (reduce merge headers
;;             (make-authorization-headers method signing-key headers))))
;;
;; (def ^:dynamic *chef-server-url* (env :chef-server-url))
;;
;; (defmacro with-chef-server [new-chef-server & body]
;;   `(binding [chef-server-url ~new-chef-server]
;;      ~@body))
;;
;; (defn make-request [method endpoint]
;;   (http/request {:url (str *chef-server-url* endpoint)
;;                  :method method
;;                  :keepalive 1000}))
