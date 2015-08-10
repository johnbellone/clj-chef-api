(ns spoon.nodes
  (:require [spoon.core :as client]))

(defn get-nodes [org & [options]]
  (client/api-request :get "/organizations/%s/nodes" [org] options))

(defn get-node [org node & [options]]
  (client/api-request :get "/organizations/%s/nodes/%s" [org node] options))

(defn update-node
  [org node & [options]]
  (let [data (assoc (get-node org node options) :data (:data options))]
    (client/api-request :put "/organizations/%s/nodes/%s" [org node] (assoc options {:data data}))))

(defn update-node-environment
  "Update the environment of a node.

  Required parameters:
        :org    - Organization of the target node.
        :node   - Identifier for the target node.
        :environment - Name of the new Chef environment."
  [org node environment & [options]]
  (update-node org node (assoc options :chef_environment environment)))

(defn delete-node [org node & [options]]
  (client/api-request :delete "/organizations/%s/nodes/%s" [org node] options))
