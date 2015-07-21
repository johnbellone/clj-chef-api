(ns chef-api-client.util.crypto-test
  (:require [clojure.test :refer :all]
            [clojure.string :as str]
            [chef-api-client.util.crypto :refer :all]))

(use-fixtures :once (fn [f] (init-providers) (f)))

(def from-ruby
  "Example signature using the code from Chef's Mixlib::Authentication. We'll
  have to match this to have the chef rest api accept our requests:

       %w[openssl base64].each {|lib| require lib}
       key = OpenSSL::PKey::RSA.new('test/fixtures/client.pem')
       Base64.encode64(key.private_encrypt('foobar')).chomp"

  (str "pzLwx6Y8G2Oh35+gky/D/LPtQOdIsMc/itXHBMjeV0prLWVtiJCnerNKSeGQ" \newline
       "cQq94vpmn8RAsdRjsvdNTGlnZ0gmX7+uVzdQzpGIHmphHn65LewcHig584RV" \newline
       "g6fqikjlbwuggA7M3JwCvvWvVteY4TBbEGoThGTNjU2nAbk5fvf8uKPpNmfM" \newline
       "qfofcnPHwt3iPsHPorQ7tgFF9Q8nUIZ5mkS0K2uExf7GuzieyTqUwS2O1zEW" \newline
       "DDwaOE/qTCWG3J4uNN/bxL0LNpcAFhm+8IQoMm/iM3oTJKwjpH0NTr1QerGj" \newline
       "svUuPHrtf/Fd44GipsnTOFAXRvXHtDfr3RHma37aAg=="))

(deftest encryption-test
  (is (= (str/replace from-ruby #"\n" "")
         (encrypt "foobar" (read-pem "test/fixtures/client.pem")))))
