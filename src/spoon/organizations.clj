(ns spoon.organizations
  (:require [clj-spoon.core :as client]))

(defn get-organizations [& [options]]
  (client/api-request :get "/organizations"))

(defn get-organization [name & [options]]
  (client/api-request :get "/organizations/%s" [name] options))

(defn delete-organization [name & [options]]
  (client/api-request :delete "/organizations/%s" [name] options))

(defn create-organization [name full-name & [options]]
  (client/api-request :post "/organizations" (assoc options {:data {:name name :full_name full-name}})))

(defn update-organization [name full-name & [options]]
  (client/api-request :put "/organizations/%s" (assoc options {:data {:name name :full_name full-name}})))
