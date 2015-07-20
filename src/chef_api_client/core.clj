(ns chef-api-client.core
  (:require
	[clojure.string :as str]
    [cheshire.core :as json]
    [org.httpkit.client :as http]
    [clj-time.core :as time]
    [environ.core :refer [env]]
	[chef-api-client.util.crypto :as crypto]))

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
             "Hashed Path:" (crypto/digest Path) \newline
             "X-Ops-Content-Hash:" X-Ops-Content-Hash \newline
             "X-Ops-Timestamp" X-Ops-Timestamp \newline
             "X-Ops-UserId:" X-Ops-UserId \newline)]
    (split-x-auth (-> canonical-headers
					  (crypto/encrypt secret-key)
					  (str/trim-newline)))))

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
