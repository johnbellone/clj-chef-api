# clj-spoon [![Build Status](https://travis-ci.org/johnbellone/clj-spoon.svg)](https://travis-ci.org/johnbellone/clj-spoon)

[Chef Server API][1] client written in [Clojure][2].

## Usage
When using the `lein repl` a default environment is set up based on
your current username and home directory. These can be overriden using
environment variables (CHEF_SERVER_HOST, CHEF_CLIENT_NAME and
CHEF_CLIENT_KEY).
```clojure
user=> (println default-info)
{:chef-host manage.chef.io, :client-name jbellone, :client-key /Users/jbellone/.chef/jbellone.pem}
nil
```

[1]: https://chef.io
[2]: http://clojure.org
