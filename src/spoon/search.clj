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

(defn lazy-search
  "Internal: Make a version of a search function that returns a lazy sequnce of
  all nodes, rather than taking pagination options. Again, prefer using the *-seq
  functions in this namespace rathern than calling this directly."
  ([search] (lazy-search search 64))
  ([search nrows]
   (fn [org q & [options]]
     (letfn [(step [idx done pending]
               (lazy-seq
                 (if (seq pending)
                   (cons (first pending) (step idx false (rest pending)))
                   (when-not done
                     (let [more (:rows (search org q {:start idx, :rows nrows} options))]
                       (step (+ idx nrows)
                             (< (count more) nrows)
                             more))))))]
       (step 1 false [])))))

(defmacro make-search
  "Create a new search of index type. Creates both pagination enabled and lazy-seq versions."
  [sym index]
  `(do
    (defn ~sym
      ~(format
         "Search for %s within org. Specify a search query and pagination options
         (rows, start, sort) as per https://docs.chef.io/knife_search.html#syntax."
         sym)
      [org# q# pagination# & [options#]]
      (search-index
        {:index ~index
         :org org#
         :q q#
         :pagination pagination#
         :options options#}))
    (def ^{:arglists '([org q & [opts]])
           :doc ~(format "Lazy search for %s, returns results of query q in org." ~index)}
      ~(symbol (str sym "-seq")) (lazy-search ~sym))))

(make-search nodes "node")

(defn
  ^{:deprecated "0.3.3"}
  get-nodes
  [org & [options]]
  (client/api-request :get "/organizations/%s/search/client" [org] options))

(make-search clients "client")

(defn
  ^{:deprecated "0.3.3"}
  get-clients
  [org & [options]]
  (client/api-request :get "/organizations/%s/search/client" [org] options))

(make-search roles "role")

(defn
  ^{:deprecated "0.3.3"}
  get-roles
  [org & [options]]
  (client/api-request :get "/organizations/%s/search/role" [org] options))

(make-search environments "environment")
