(defproject com.bloomberg.inf/spoon "0.3.0-SNAPSHOT"
  :description "Chef Server API client written in Clojure."
  :url "https://github.com/johnbellone/spoon"
  :license {:name "Apache 2.0"
            :url "http://www.apache.org/licenses/LICENSE-2.0"}
  :dependencies [[org.clojure/clojure "1.7.0"]
                 [org.clojure/tools.logging "0.3.1"]
                 [org.clojure/data.codec "0.1.0"]
                 [cheshire "5.5.0"]
                 [me.raynes/fs "1.4.6"]
                 [pandect "0.5.2" :exclusions [org.bouncycastle/bcprov-jdk15on]]
                 [clj-time "0.10.0"]
                 [http-kit "2.1.19"]
                 [org.bouncycastle/bctsp-jdk15on "1.46"
                  :exclusions [org.bouncycastle/bcmail-jdk15onb
                               org.bouncycastle/bcprov-jdk15on]]
                 [org.bouncycastle/bcpkix-jdk15on "1.52"]
                 [clj-logging-config "1.9.12"]]
  :repositories [["clojars" {:url "https://clojars.org/repo"
                             :username :env/clojars_username
                             :password :env/clojars_password}]]
  :profiles {:uberjar {:aot :all}
             :dev {:source-paths ["dev"]
                   :dependencies [[environ "1.0.0"]]}})
