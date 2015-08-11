# clj-spoon [![Build Status](https://travis-ci.org/johnbellone/clj-spoon.svg)](https://travis-ci.org/johnbellone/clj-spoon)

[Chef Server API][1] client written in [Clojure][2].

## Usage
When using the `lein repl` a default environment is set up based on
your current username and home directory. These can be overriden using
environment variables (*CHEF_SERVER_HOST*, *CHEF_CLIENT_NAME* and
*CHEF_CLIENT_KEY*).
```clojure
user=> (println *client-info*)
{:chef-host manage.chef.io, :client-name jbellone, :client-key /Users/jbellone/.chef/jbellone.pem}
nil
```
If you have an on-premises Chef Server you are going to need to
override the client information. For example, let's assume that the
Chef Server API that we're operating on is located at
_chef.internal.corporate.com_. We are able to override the host in
three ways:

1. Set the environment variable *CHEF_SERVER_HOST* prior to running
the `lein repl` command.
2. Set the :chef-server-host in our [Clojure development profile][3].
3. Use the Clojure macro `with-chef-server` which overrides only the
:chef-server-host key in the _client-info_ object.

[1]: https://chef.io
[2]: http://clojure.org
[3]: https://github.com/weavejester/environ#example-usage
