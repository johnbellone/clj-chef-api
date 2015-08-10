(ns spoon.data
  (:require [spoon.core :as client]))

(defn get-data [org & [options]]
  (let [data (client/api-request :get "/organizations/%s/data" [org] options)]
    (map name (keys data))))

(defn get-data-bag [org bag-name & [options]]
  (client/api-request :get "/organizations/%s/data/%s" [org bag-name] options))

(defn create-data-bag [org bag-name & [options]]
  (client/api-request :post "/organizations/%s/data" [org] (assoc options :body {:name bag-name})))

(defn delete-data-bag [org bag-name & [options]]
  (client/api-request :delete "/organizations/%s/data/%s" [org bag-name]))

(defn get-data-bag-item [org bag-name item-name & [options]]
  (client/api-request :get "/organizations/%s/data/%s/%s" [org bag-name item-name] options))

(defn create-data-bag-item [org bag item-name item-data & [options]]
  (let [data (merge {} {:id item-name}
                    (when-let [item-data (or item-data false)]
                      item-data))]
    (client/api-request :post "/organizations/%s/data/%s" [org bag-name] (assoc options :body data))))

(defn update-data-bag-item [org bag item-name item-data & [options]]
  (when-let [data (or item-data false)]
    (client/api-request :put "/organizations/%s/data/%s/%s" [org bag-name item-name] (assoc options :body data))))

(defn delete-data-bag-item [org bag item-name & [options]]
  (client/api-request :delete "/organizations/%s/data/%s/%s" [org bag item] options))
