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
           {"X-Ops-Authorization-1" "aaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaa"
            "X-Ops-Authorization-2" "aaaaaaaaaaaaaaaaaaaaazzzzzzzzzzzzzzzzzzzzzzzzzzzzzzzzzzzzzz"
            "X-Ops-Authorization-3" "zzzzzzzzzzzzzzzzzzzzzzzzzzzzzzzzzzzzzzzzzz00000000000000000"
            "X-Ops-Authorization-4" "00000000000000000000000000000000000000000000000000000"}))))

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
    (is (= "YtBWDn1blGGuFIuKksdwXzHU9oE=")
        (crypto/digest "/organizations/clownco"))
    ;; /spec/mixlib/authentication/mixlib_authentication_spec.rb#L241
    (is (= "DFteJZPVv6WKdQmMqZUQUumUyRs=")
        (crypto/digest "Spec Body")))

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
                   X-Ops-UserId:spec-user
                   ")
          make-request-headers
          (ns-resolve 'spoon.core 'make-request-headers)]
      (is (= canonical-request
             (canonicalize-request raw-request)))
      #_(is (= canonical-request
             (make-request-headers (:http-method raw-request)
                                   (merge {:client-name (:user-id raw-request)
                                           :client-key "test/fixtures/mixlib.pem"}
                                          raw-request)))))))
