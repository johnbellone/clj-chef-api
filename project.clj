(defproject com.bloomberg.infrastructure/chef-api-client "0.1.0"
  :description "Chef API client written in Clojure."
  :url "https://github.com/johnbellone/clj-chef-api-client"
  :license {:name "Apache 2.0"
            :url "http://www.apache.org/licenses/LICENSE-2.0"}
  :uberjar-name "chef-api-client.jar"
  :dependencies [[org.clojure/clojure "1.6.0"]
                 [org.clojure/tools.cli "0.3.1"]
                 [org.clojure/tools.logging "0.3.1"]
                 [org.clojure/data.codec "0.1.0"]
                 [cheshire "5.5.0"]
                 [environ "1.0.0"]
                 [me.raynes/fs "1.4.6"]
                 [pandect "0.5.2" :exclusions [org.bouncycastle/bcprov-jdk15on]]
                 [clj-time "0.10.0"]
                 [http-kit "2.1.19"]

                 [org.bouncycastle/bctsp-jdk15on "1.46"
                  :exclusions [org.bouncycastle/bcmail-jdk15onb
                               org.bouncycastle/bcprov-jdk15on]]
                 [org.bouncycastle/bcpkix-jdk15on "1.52"]]
  :profiles
    {:dev     {:env {:chef-server-url "https://127.0.0.1:8443/"}}
     :uberjar {:aot :all}})
