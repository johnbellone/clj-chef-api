(ns spoon.policies
  (:require [spoon.core :as client]))

(defn get-policy-group [org group & [options]]
  (client/api-request :get "/organizations/%s/policy_groups/%s" [org group] options))

(defn get-policies [org group & [options]]
  (client/api-request :get "/organizations/%s/policy_groups/%s/policies" [org group] options))

(defn get-policy [org group name & [options]]
  (client/api-request :get "/organizations/%s/policy_groups/%s/policies/%s" [org group name] options))
