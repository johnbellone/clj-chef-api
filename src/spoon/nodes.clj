(ns spoon.nodes
  (:require [spoon.core :as client]))

(defn get-nodes [org & [options]]
  (let [nodes (client/api-request :get "/organizations/%s/nodes" [org] options)]
    (map name (keys nodes))))

(defn get-node [org node & [options]]
  (client/api-request :get "/organizations/%s/nodes/%s" [org node] options))

(defn update-node
  [org node data & [options]]
  (let [old-data (get-node org node options)
        new-data {:body (merge old-data data)}]
    (client/api-request :put "/organizations/%s/nodes/%s" [org node] (merge options new-data))))

(defn update-node-environment
  "Update the environment of a node.

  Required parameters:
        :org    - Organization of the target node.
        :node   - Identifier for the target node.
        :environment - Name of the new Chef environment."
  [org node environment & [options]]
  (update-node org node {:chef_environment environment} options))

(defn delete-node [org node & [options]]
  (client/api-request :delete "/organizations/%s/nodes/%s" [org node] options))
