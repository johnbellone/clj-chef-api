(ns spoon.nodes
  (:require [spoon.core :as client]))

(defn get-nodes [org & [options]]
  (client/api-request :get "/organizations/%s/nodes" [org] options))

(defn get-node [org node & [options]]
  (client/api-request :get "/organizations/%s/nodes/%s" [org node] options))

(defn delete-node [org node & [options]]
  (client/api-request :delete "/organizations/%s/nodes/%s" [org node] options))
