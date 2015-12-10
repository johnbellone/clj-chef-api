(ns spoon.environments
  (:require [spoon.core :as client]))

(defn get-environments [org & [options]]
  (keys (client/api-request :get "/organizations/%s/environments" [org] options)))

(defn get-environment-nodes [org environment & [options]]
  (let [nodes (client/api-request :get "/organizations/%s/environments/%s/nodes" [org environment] options)]
    (map name (keys nodes))))

(defn get-environment-cookbooks [org environment & [options]]
  (client/api-request :get "/organizations/%s/environments/%s/cookbooks" [org environment] options))

(defn get-environment [org environment & [options]]
  (client/api-request :get "/organizations/%s/environments/%s" [org environment] options))

(defn delete-environment [org environment & [options]]
  (client/api-request :delete "/organizations/%s/environments/%s" [org environment] options))
