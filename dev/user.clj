(ns user
  (:use [spoon.core]
        [spoon.nodes])
  (:require [cheshire.core :as json]
            [clojure.pprint :refer :all]
            [clojure.set :as set]
            [clojure.test :refer [run-tests run-all-tests]]
            [environ.core :refer [env]]))

(def default-info
  (let [client-name (or (env :chef-client-name) (System/getProperty "user.name"))
        client-key (or (env :chef-client-key) (str (System/getProperty "user.home") "/.chef/" client-name ".pem"))
        chef-server (or (env :chef-server-host) "manage.chef.io")]
    (spoon.core/client-info chef-server client-name client-key)))

(defn flip-node-environment
  "Modifies an existing node's environment."
  [org node environment]
  (spoon.nodes/update-node-environment org node environment default-info))
