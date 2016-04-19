(ns spoon.search
  "Exposes chef search endpoints"
  (:require [spoon.core :as client]))

(defn search-index
  "Internal: Generic search request, takes the index to use, along with the
  organization to search, the search query and pagination options. Prefer using
  the index specific functions in this namespace rather than using this
  directly."
  [{:keys [index org q pagination options]
    :or {pagination {}, options {}}}]
  {:pre [(#{"node" "client" "role" "environment" "policyfiles"} index)]}
  (let [endpoint (str "/organizations/%s/search/" index)
        params (assoc options :query (assoc pagination :q q))]
    (client/api-request :get endpoint [org] params)))

(defn nodes
  "Search for nodes withing org. Specify a search query and pagination options
  (rows, keys, sort) as per https://docs.chef.io/knife_search.html#syntax."
  [org q pagination & [options]]
  (search-index
    {:index "node"
     :org org
     :q q
     :pagination pagination
     :options options}))

(defn
  ^{:deprecated "0.3.3"}
  get-nodes
  [org & [options]]
  (client/api-request :get "/organizations/%s/search/client" [org] options))

(defn clients
  "Search for clients withing org. Specify a search query and pagination options
  (rows, keys, sort) as per https://docs.chef.io/knife_search.html#syntax."
  [org q pagination & [options]]
  (search-index
    {:index "client"
     :org org
     :q q
     :pagination pagination
     :options options}))

(defn
  ^{:deprecated "0.3.3"}
  get-clients
  [org & [options]]
  (client/api-request :get "/organizations/%s/search/client" [org] options))

(defn roles
  "Search for roless withing org. Specify a search query and pagination options
  (rows, keys, sort) as per https://docs.chef.io/knife_search.html#syntax."
  [org q pagination & [options]]
  (search-index
    {:index "role"
     :org org
     :q q
     :pagination pagination
     :options options}))

(defn
  ^{:deprecated "0.3.3"}
  get-roles
  [org & [options]]
  (client/api-request :get "/organizations/%s/search/role" [org] options))


(defn environments
  "Search for environments withing org. Specify a search query and pagination options
  (rows, keys, sort) as per https://docs.chef.io/knife_search.html#syntax."
  [org q pagination & [options]]
  (search-index
    {:index "environment"
     :org org
     :q q
     :pagination pagination
     :options options}))
