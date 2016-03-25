
boot task for Cirru Sepal
----

Notice: this task is using a standalone watcher since not well intergrated for `boot-reload`. Still WIP.

### Usage

[![Clojars Project](https://img.shields.io/clojars/v/cirru/boot-cirru-sepal.svg)](https://clojars.org/cirru/boot-cirru-sepal)

```clojure
(require '[cirru-sepal.core :refer :all])

(deftask just-compile []
  (cirru-sepal :paths ["cirru-demo"]))

(deftask watch-compile []
  (cirru-sepal :paths ["cirru-demo"] :watch true))

(deftask watch-compile []
  (cirru-sepal :paths ["cirru-demo"] :watch true :alone true))
```

* `:paths`, example `["cirru-src"]`
* `:watch` bool
* `:alone` bool, use a loop to force running

Process will mostly quit after watching process started.
Option `:alone` is designed to add a piece of code to bypass quitting:

```clojure
(if alone (loop []
    (Thread/sleep 400)
    (recur)))
```

### License

MIT
