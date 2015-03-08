(ns spoon.core-test
  (:use clojure.test)
  (:require [spoon.core :as spoon]))

(deftest request-header-user-agent
  (let [request (spoon/make-request :get "/")]
    (do (is (empty? (:query-params request)))
        (is (contains? (:headers request) "User-Agent"))
        (is (= (get (:headers request) "User-Agent") "Spoon")))))
(deftest request-header-x-ops-sign)
(deftest request-header-x-ops-userid)
(deftest request-header-x-ops-timestamp)
(deftest request-header-x-ops-content-hash)
(deftest request-header-x-ops-authorization-n)
(deftest request-header-x-chef-version)
