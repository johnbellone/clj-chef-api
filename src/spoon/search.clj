(ns spoon.search
  (:require [spoon.core :as client]))

(defn search-index
  [{:keys [index org q pagination options]
    :or {pagination {}, options {}}}]
  {:pre [(#{"node" "client" "role"} index)]}
  (let [endpoint (str "/organizations/%s/search/" index)
        params (assoc options :query (assoc pagination :q q))]
    (client/api-request :get endpoint [org] params)))

(defn nodes
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

(defn role
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
