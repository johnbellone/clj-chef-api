(ns spoon.organizations
  (:require [clj-spoon.core :as client]
            [clj-time.core :as time]
            [clj-time.format :as time.fmt]))

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

(defn create-organization-user-key
  [org user-name key-name public-key & [options]]
  (let [expiry (or expiry "infinity")
        data (assoc {} :name key-name :public_key public-key :expiration_date expiry)]
    (client/api-request :post "/organizations/%s/users/%s/keys" [org user-name] (merge options {:body data}))))

(defn delete-organization-user-key
  [org user-name key-name & [options]]
  (client/api-request :delete "/organizations/%s/users/%s/keys/%s" [org user-name key-name] options))

(defn update-organization-user-key
  [org key-name & [options]]
  (let [data (merge {}
                    (when-let [public-key (or (:public-key options) nil)]
                      {:public_key public-key})
                    (when-let [expiry (or (:expiration-date options) nil)]
                      {:expiration_date expiry})
                    (when-let [new-name (or (:key-name options) nil)]
                      {:name new-name}))]
    (if-not (= {} data)
      (client/api-request :put "/organizations/%s/users/%s/keys/%s" [org user-name key-name]
                          (merge options {:body data})))))
