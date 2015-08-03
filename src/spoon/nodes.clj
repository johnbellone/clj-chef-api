(ns spoon.nodes
  (:require [spoon.core :as client]))

(defn list-nodes []
  (client/request "nodes"))

(defn get-node
  [hostname]
  (client/request :get (format "nodes/%s" hostname)))

(defn create-node
  [hostname & params]
  (client/request :post (format "nodes/%s" hostname) params))

(defn delete-node
  [hostname]
  (client/request :delete (format "nodes/%s" hostname)))

(defn edit-node
  [hostname & params]
  (client/request :put (format "nodes/%s" hostname) params))
