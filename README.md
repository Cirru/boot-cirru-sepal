
boot task for Cirru Sepal
----

### Usage

[![Clojars Project](https://img.shields.io/clojars/v/cirru/boot-cirru-sepal.svg)](https://clojars.org/cirru/boot-cirru-sepal)

[cirru/boot-cirru-sepal "0.1.10"]

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
* `.ir` for C**ir**ru EDN, see https://github.com/Cirru/vectors-format

### License

MIT
