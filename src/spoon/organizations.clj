(ns spoon.organizations
  (:require [spoon.core :as client]
            [clj-time.core :as time]
            [clj-time.format :as time.fmt]))

(defn get-organizations [& [options]]
  (client/api-request :get "/organizations" [] options))

(defn get-organization [name & [options]]
  (client/api-request :get "/organizations/%s" [name] options))

(defn delete-organization [name & [options]]
  (client/api-request :delete "/organizations/%s" [name] options))

(defn create-organization [name full-name & [options]]
  (client/api-request :post "/organizations" (assoc options {:data {:name name :full_name full-name}})))

(defn update-organization [name full-name & [options]]
  (client/api-request :put "/organizations/%s" (assoc options {:data {:name name :full_name full-name}})))

(defn create-organization-user-key
  [org user-name key-name public-key expiry & [options]]
  (let [expiry (or expiry "infinity")
        data (assoc {} :name key-name :public_key public-key :expiration_date expiry)]
    (client/api-request :post "/organizations/%s/users/%s/keys" [org user-name] (merge options {:body data}))))

(defn delete-organization-user-key
  [org user-name key-name & [options]]
  (client/api-request :delete "/organizations/%s/users/%s/keys/%s" [org user-name key-name] options))

(defn update-organization-user-key
  [org user-name key-name & [options]]
  (let [data (merge {}
                    (when-let [public-key (:public-key options false)]
                      {:public_key public-key})
                    (when-let [expiry (:expiration-date options false)]
                      {:expiration_date expiry})
                    (when-let [new-name (:key-name options false)]
                      {:name new-name}))]
    (if-not (= {} data)
      (client/api-request :put "/organizations/%s/users/%s/keys/%s" [org user-name key-name]
                          (merge options {:body data})))))
