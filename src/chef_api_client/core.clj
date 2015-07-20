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
            [(keyword (str "X-Ops-Authorization-" (inc n)))
             (apply str s)])]
    (into {} (map-indexed header-n (partition-all 59 token)))))

(defn make-authorization-headers
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

(defn make-request-headers
  [client-name client-key & [options]]
  (let [signing-key (crypto/read-pem client-key)
		method (str/upper-case (:method options))
		host (:host options)
		body (:body options)
		headers (merge default-headers
				  (:headers options)
				  {:Host host
				   :X-Chef-UserId client-name
				   :X-Ops-Timestamp (time/now)
				   :X-Content-Hash (crypto/digest body)})]
	(merge headers
		   (make-authorization-headers method signing-key headers))))

(defn inspect-headers
  [m]
  (doall (for [[k v] (sort-by first m)]
		   (println (format "%s: %s" (name k) v)))) nil)

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
