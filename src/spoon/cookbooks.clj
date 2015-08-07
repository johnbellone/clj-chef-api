(ns spoon.cookbooks
  (:require [spoon.core :as client]))

(defn get-cookbooks [org & [options]]
  (client/api-request :get "/organizations/%s/cookbooks" [org] options))

(defn get-cookbook [org cookbook version & [options]]
  (if version
    (client/api-request :get "/organizations/%s/cookbooks/%s" [org cookbook version] options)
    (client/api-request :get "/organizations/%s/cookbooks" [org cookbook] options)))

(defn latest-cookbook [org cookbook & [options]]
  (client/api-request :get "/organizations/%s/cookbooks/_latest" [org cookbook] options))
