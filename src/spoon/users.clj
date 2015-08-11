(ns spoon.users
  (:require [spoon.core :as client]))

(defn get-users
  [& [options]]
  (let [users (client/api-request :get "/users" options)]
    (map name (key users))))

(defn get-user
  [name & [options]]
  (name (client/api-request :get "/users/%s" [name] options)))

(defn delete-user
  [name & [options]]
  (client/api-request :delete "/users/%s" [name] options))

(defn create-user
  [name & [options]]
  (let [data (merge {}
                    {:admin (:admin options false)}
                    (when-let [public-key (:public-key options false)]
                      {:public_key public-key}))]
    (client/api-request :post "/users" (merge options {:body data}))))
