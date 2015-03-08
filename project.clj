(defproject com.bloomberg.platform/spoon "0.1.0"
  :description "Clojure client to access Chef Server."
  :url "https://github.com/johnbellone/spoon"
  :license {:name "Apache 2.0"
            :url "http://www.apache.org/licenses/LICENSE-2.0"}
  :uberjar-name "chef-spoon.jar"
  :dependencies [[org.clojure/clojure "1.6.0"]
                 [org.clojure/tools.cli "0.3.1"]
                 [org.clojure/tools.logging "0.3.1"]
                 [cheshire "5.4.0"]
                 [environ "1.0.0"]
                 [me.raynes/fs "1.4.6"]
                 [http-kit "2.1.18"]]
  :profiles
  {:uberjar {:aot :all}})
