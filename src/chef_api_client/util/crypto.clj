(ns chef-api-client.util.crypto
  (:require
    [clojure.data.codec.base64 :as b64]
    [pandect.algo.sha1 :as algo])
  (:import
    [java.security Security KeyFactory]
    [java.security.spec RSAPrivateKeySpec]
    [javax.crypto Cipher]
    [org.bouncycastle.crypto.util PrivateKeyFactory]
    [org.bouncycastle.jce.provider BouncyCastleProvider]
    [org.bouncycastle.openssl PEMParser]))

;;; Hash/Encoding utilities

(def ^{:arglists '([data])
       :doc "Encode to base64 as a UTF-8 string."}
  b64-string
  (comp #(String. % "UTF-8") b64/encode))

(def ^{:arglists '([value secret-key])
       :doc "Return a base64 encoded hmac-sha1 token from value. Signed with
            secret-key."}
  hmac-sha
  (comp b64-string algo/sha1-hmac-bytes))

(def ^{:arglists '([value])
       :doc "Return a base64 encoded string SHA1 digest from value." }
  digest
  (comp b64-string algo/sha1-bytes))

;;; BC Crypto

(defn init-providers "Add BouncyCastle to JCE" []
  (Security/addProvider (BouncyCastleProvider.)))

(defn- ^:nodoc pem->bc-pkey
  "Internal: Extract private key from the OpenSSL pem file at the given path."
  [path]
  (-> path
      (java.io.FileReader.)
      (PEMParser.)
      (.readObject)
      (.getPrivateKeyInfo)
      (PrivateKeyFactory/createKey)))

(defn read-pem
  "Read private key from PEM file and convert to a form usable by JCE. Can be
  passed to the encrypt function in this namespace."
  [path]
  (let [{:keys [exponent modulus]} (into {} (seq (bean (pem->bc-pkey path))))
        factory (KeyFactory/getInstance "RSA")
        spec (RSAPrivateKeySpec. modulus exponent)]
    (.generatePrivate factory spec)))

(defn encrypt
  "Encrypt the given data s, using the provided private key."
  [s pkey]
  ;; NOTE: Use of private key and not public is intentional. This is what Chef's
  ;; Mixlib does rather than using the sign/verify methods. See
  ;; chef-api-client.util.crypto-test/from-ruby for more details and the
  ;; corresponding Ruby snipit.
  (let [cipher (doto (Cipher/getInstance "RSA/ECB/PKCS1Padding" "BC")
                 (.init Cipher/ENCRYPT_MODE pkey))]
    (-> s
        (.getBytes)
        (->> (.doFinal cipher))
        b64-string)))
