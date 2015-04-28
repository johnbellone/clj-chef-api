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
                 [clj-crypto "1.0.2"]
                 [cheshire "5.4.0"]
                 [environ "1.0.0"]
                 [me.raynes/fs "1.4.6"]
                 [pandect "0.5.1"]
                 [clj-time "0.9.0"]
                 [http-kit "2.1.18"]]
  :profiles
  {:dev {:env {:chef-server-url "https://127.0.0.1:8443/"}}
   :uberjar {:aot :all}})
