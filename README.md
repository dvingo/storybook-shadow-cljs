This template utilizes GNU Make 4.x. You'll need to install it first 
before executing `make`.

# Start dev

In one terminal:

```bash
make
```
this runs `yarn` and starts the shadow-cljs server.

Wait for this to complete, then:

In another terminal run:
```bash
make fe
```
This starts the shadow cljs watches.

Please see the `shadow-cljs.edn` file for ports used for development builds.

If any of those ports are used already shadow-cljs will try different ports so please see the console output 
by shadow-cljs.


## Editor setup

In your editor:
add 2 repls:

### frontend repl:

nREPL remote:

  localhost:$port
  
The $port defaults to 9000 but may be different if 9000 is already in use.

Using this repl you connect to the various clojurescript builds using `(shadow/repl :build-id)`

### backend repl:

nREPL local

alias: dev

JVM Args: `-Dguardrails.enabled=true`

The `-Dguardrails.enabled=true` turns on guardrails instrumentation of guardrails spec'd functions, which is a wrapper
of Clojure spec that makes instrumentation and production-time elision (for performance and size) much easier.

start backend repl, then:

```clojure
(start) ;; (user/start)
```
This uses mount to start web server.

### Clojure webserver.

The clojure webserver listens on port 8085 by default - this is specified in `src/main/config/defaults.edn`

http://localhost:8085

after opening the browser, then you can connect a clojurescript repl to the app:

```clojure
(shadow/repl :main)
```

## Shadow cljs dashboard

http://127.0.0.1:9630



