(ns spoon.core
  (:require [clj-http.client :as http]
            [cheshire.core :as json]
            [clojure.string :as str]
            [org.httpkit.client :as http]
            [environ.core :refer [env]]))

(def ^:dynamic chef-server-url (env :chef-server-url))

(def request-headers
  {:x-chef-version "12.1.0"})

(defn make-headers [& options]
  )

(defn make-request [uri]
  (let [{:keys [status headers body error] :as response} @(http/get chef-server-url)]
    (if error
      (log/error "Failed, exception raised: " error))))

(defmacro with-chef-server [new-chef-server & body]
  `(binding [chef-server-url ~new-chef-server]
     ~@body))
