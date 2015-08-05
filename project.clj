(defproject com.bloomberg.platform/spoon "0.1.0"
  :description "Chef Server API client written in Clojure."
  :url "https://github.com/johnbellone/clj-chef-server-client"
  :license {:name "Apache 2.0"
            :url "http://www.apache.org/licenses/LICENSE-2.0"}
  :uberjar-name "chef-server-client.jar"
  :plugins [[lein-bin "0.3.4"]]
  :dependencies [[org.clojure/clojure "1.6.0"]
                 [org.clojure/tools.cli "0.3.1"]
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
  :profiles {:uberjar {:aot :all}
             :dev {:source-paths ["dev"] }})
