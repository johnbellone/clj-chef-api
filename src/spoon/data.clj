(ns spoon.data
  (:require [spoon.core :as client]))

(defn get-data [org bag & [options]]
  (if bag
    (client/api-request :get "/organizations/%s/data/%s" [org bag] options)
    (client/api-request :get "/organizations/%s/data" [org] options)))

(defn get-data-item [org bag item & [options]]
  (client/api-request :get "/organizations/%s/data/%s/%s" [org bag item] options))
