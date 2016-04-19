(ns spoon.search
  (:require [spoon.core :as client]))

(defn
  ^{:deprecated "0.3.3"}
  get-nodes
  [org & [options]]
  (client/api-request :get "/organizations/%s/search/client" [org] options))

(defn
  ^{:deprecated "0.3.3"}
  get-clients
  [org & [options]]
  (client/api-request :get "/organizations/%s/search/client" [org] options))

(defn
  ^{:deprecated "0.3.3"}
  get-roles
  [org & [options]]
  (client/api-request :get "/organizations/%s/search/role" [org] options))
