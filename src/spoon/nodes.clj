(ns spoon.nodes
  (:require [spoon.core :as client]))

(defn get-nodes [org & [options]]
  (client/api-request
    (merge options {:method :get, :path (format "/organizations/%s/nodes" org)})))

(defn get-node [org node & [options]]
  (client/api-request
    (merge options {:method :get, :path (format "/organizations/%s/nodes/%s" org node)})))

(defn delete-node [org node & [options]]
  (client/api-request
    (merge options {:method :delete, :path (format "/organizations/%s/nodes/%s" org node)})))
