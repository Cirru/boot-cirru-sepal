
boot task for Cirru Sepal
----

### Usage

[![Clojars Project](https://img.shields.io/clojars/v/cirru/boot-cirru-sepal.svg)](https://clojars.org/cirru/boot-cirru-sepal)

```clojure
(require '[cirru-sepal.core :refer :all])

(comp
  (watch)
  (transform-cirru)
  (cljs))
```

Supported extensions:

* `.cirru`
* `.json`
* `.edn`

### License

MIT
