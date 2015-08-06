(ns spoon.core-test
  (:require
    [clojure.string :as str]
    [clojure.test :refer :all]
    [spoon.util.crypto :as crypto]
    [spoon.core :refer :all]))

(defn- heredoc [doc]
  (str/replace doc #"(?ms)^\s+" ""))

(deftest test-split-x-auth
  (let [message (str/join (mapcat (partial apply repeat) [[80 \a] [80 \z] [70 \0]]))]
    (is (= (split-x-auth message)
           {"X-Ops-Authorization-1" "aaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaa"
            "X-Ops-Authorization-2" "aaaaaaaaaaaaaaaaaaaazzzzzzzzzzzzzzzzzzzzzzzzzzzzzzzzzzzzzzzz"
            "X-Ops-Authorization-3" "zzzzzzzzzzzzzzzzzzzzzzzzzzzzzzzzzzzzzzzz00000000000000000000"
            "X-Ops-Authorization-4" "00000000000000000000000000000000000000000000000000"}))))

(deftest chef-mixlib-specs
  (testing "Creates content hashes correctly"
    ;; https://github.com/chef/mixlib-authentication/blob/c7ad2f67c6feb508b2567c495abec13f018186d5
    ;; /spec/mixlib/authentication/mixlib_authentication_spec.rb#L245
    (is (= "YtBWDn1blGGuFIuKksdwXzHU9oE="
           (crypto/digest "/organizations/clownco")))
    ;; /spec/mixlib/authentication/mixlib_authentication_spec.rb#L241
    (is (= "DFteJZPVv6WKdQmMqZUQUumUyRs="
           (crypto/digest "Spec Body"))))

  (testing "v1.1 example"
    (let [raw-request
          {:body "Spec Body"
           :client-name "spec-user"
           :method "post"
           :timestamp "2009-01-01T12:00:00Z"
           :path "/organizations/clownco"}

          canonical-request
          (heredoc "Method:POST
                   Hashed Path:YtBWDn1blGGuFIuKksdwXzHU9oE=
                   X-Ops-Content-Hash:DFteJZPVv6WKdQmMqZUQUumUyRs=
                   X-Ops-Timestamp:2009-01-01T12:00:00Z
                   X-Ops-UserId:spec-user")

          expected-signed-result
          {"X-Ops-Content-Hash"    "DFteJZPVv6WKdQmMqZUQUumUyRs="
           "X-Ops-UserId"          "spec-user"
           "X-Ops-Sign"            "algorithm=sha1;version=1.0;"
           "X-Ops-Authorization-1" "jVHrNniWzpbez/eGWjFnO6lINRIuKOg40ZTIQudcFe47Z9e/HvrszfVXlKG4"
           "X-Ops-Authorization-2" "NMzYZgyooSvU85qkIUmKuCqgG2AIlvYa2Q/2ctrMhoaHhLOCWWoqYNMaEqPc"
           "X-Ops-Authorization-3" "3tKHE+CfvP+WuPdWk4jv4wpIkAz6ZLxToxcGhXmZbXpk56YTmqgBW2cbbw4O"
           "X-Ops-Authorization-4" "IWPZDHSiPcw//AYNgW1CCDptt+UFuaFYbtqZegcBd2n/jzcWODA7zL4KWEUy"
           "X-Ops-Authorization-5" "9q4rlh/+1tBReg60QdsmDRsw/cdO1GZrKtuCwbuD4+nbRdVBKv72rqHX9cu0"
           "X-Ops-Authorization-6" "utju9jzczCyB+sSAQWrxSsXB/b8vV2qs0l4VD2ML+w=="
           "X-Ops-Timestamp"       "2009-01-01T12:00:00Z"}]
      (is (= canonical-request (canonicalize-request raw-request)))
      (is (= expected-signed-result
             (-> (make-request-headers (merge {:client-key "test/fixtures/mixlib.pem"
                                               :timestamp (:timestamp raw-request)}
                                              raw-request))
                 (select-keys (keys expected-signed-result))))))))
