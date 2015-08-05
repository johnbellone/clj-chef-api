(ns spoon.core-test
  (:require
    [clojure.string :as str]
    [clojure.test :refer :all]
    [spoon.util.crypto :as crypto]
    [spoon.core :refer :all]))

(defn- heredoc [doc]
  (str/replace doc #"(?ms)^\s+" ""))

(deftest test-split-x-auth
  (let [split-x-auth (ns-resolve 'spoon.core 'split-x-auth)
        message (str/join (mapcat (partial apply repeat) [[80 \a] [80 \z] [70 \0]]))]
    (is (= (split-x-auth message)
           {"X-Ops-Authorization-1" "aaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaa"
            "X-Ops-Authorization-2" "aaaaaaaaaaaaaaaaaaaazzzzzzzzzzzzzzzzzzzzzzzzzzzzzzzzzzzzzzzz"
            "X-Ops-Authorization-3" "zzzzzzzzzzzzzzzzzzzzzzzzzzzzzzzzzzzzzzzz00000000000000000000"
            "X-Ops-Authorization-4" "00000000000000000000000000000000000000000000000000"}))))

#_(deftest test-make-auth-headers
  (let [make-headers (ns-resolve 'spoon.core 'make-authorization-headers)]
    (crypto/init-providers)
    (is (= (make-headers "gEt"
                         (crypto/read-pem "test/fixtures/client.pem")
                         {"X-Ops-Content-Hash" "FOOBARBAZ"
                          "X-Ops-Timestamp" "IisDAtime"
                          "X-Ops-UserId" "Aclient" })
           {"X-Ops-Authorization-1" "sUgpCi32seCw/SZlMjL7vwnWeLBHBcb43vmWZrqf6gTjVM5U65lvREVZjIO"
            "X-Ops-Authorization-2" "66DnsoTXCwVCeK6CUjMZPH/wIGRKdZvkFdNfc41FSpLkHT1b4FtyK7PeiHb"
            "X-Ops-Authorization-3" "VsKpjRRc6BK72tLXtiUA1Xl6K4tVnzQGmtuqMFuF9LYDkk/ZJTBRT+9CTMI"
            "X-Ops-Authorization-4" "l37SedAVRjSDypTIED5yhpxcXHobblCyngXO/Fu5ymiNVL32UjxODGPbsP5"
            "X-Ops-Authorization-5" "jZut7rEA2GGholdGUaYgPXPsFsIRx533AFZDFjfzFx9ao7DqNYG5Bir5gtC"
            "X-Ops-Authorization-6" "2B878ohQ2IO4jjbzRo6tNuMw+IhppJjsOnqkm5OD4mhMbhQ=="}))))

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
           :user-id "spec-user"
           :http-method "post"
           :timestamp "2009-01-01T12:00:00Z"
           :file nil
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
           "X-Ops-Timestamp"       "2009-01-01T12:00:00Z"}

          make-request-headers
          (ns-resolve 'spoon.core 'make-request-headers)]
      (is (= canonical-request (canonicalize-request raw-request)))
      (is (= expected-signed-result
             (-> (make-request-headers (:http-method raw-request)
                                       (:path raw-request)
                                       (merge {:client-name (:user-id raw-request)
                                               :client-key "test/fixtures/mixlib.pem"
                                               :timestamp (:timestamp raw-request)}
                                              raw-request))
                 (select-keys (keys expected-signed-result))))))))
