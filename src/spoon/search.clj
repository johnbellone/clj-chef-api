(ns spoon.search
  (:require [spoon.core :as client]))

(defn get-nodes [org & options]
  (client/api-request :get "/organizations/%s/search/node" [org] options))

(defn get-clients [org & options]
  (client/api-request :get "/organizations/%s/search/client" [org] options))

(defn get-roles [org & options]
  (client/api-request :get "/organizations/%s/search/role" [org] options))

(defn get-users [org & options]
  (client/api-request :get "/organizations/%s/search/users" [org] options))
