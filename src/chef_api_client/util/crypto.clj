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

(def ^{:arglists '([^bytes buffer])
       :doc "Encode base64 encoded byte-array as a UTF-8 string."}
  b64-string
  (comp #(String. % "UTF-8") b64/encode))

(def ^{:arglists '([^String value secret-key])
       :doc "Return a base64 encoded hmac-sha1 token from value. Signed with
            secret-key."}
  hmac-sha
  (comp b64-string algo/sha1-hmac-bytes))

(def ^{:arglists '([^String value])
       :doc "Return a base64 encoded string SHA1 digest from value." }
  digest
  (comp b64-string algo/sha1-bytes))

;;; BC Crypto

(defn init-providers []
  (Security/addProvider (BouncyCastleProvider.)))

(defn- pem->bc-pkey
  [path]
  (-> path
	  (java.io.FileReader.)
	  (PEMParser.)
	  (.readObject)
	  (.getPrivateKeyInfo)
	  (PrivateKeyFactory/createKey)))

(defn read-pem
  [path]
  (let [{:keys [exponent modulus]} (into {} (seq (bean (pem->bc-pkey path))))
        factory (KeyFactory/getInstance "RSA")
        spec (RSAPrivateKeySpec. modulus exponent)]
    (.generatePrivate factory spec)))

(defn encrypt
  [s pkey]
  (let [cipher (doto (Cipher/getInstance "RSA/ECB/PKCS1Padding" "BC")
				 (.init Cipher/ENCRYPT_MODE pkey))]
    (-> s
        (.getBytes)
        (->> (.doFinal cipher))
        b64-string)))

