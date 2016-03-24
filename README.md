
boot task for Cirru Sepal
----

### Usage

[![Clojars Project](https://img.shields.io/clojars/v/cirru/boot-cirru-sepal.svg)](https://clojars.org/cirru/boot-cirru-sepal)

```clojure
(require '[cirru-sepal.core :refer :all])

(deftask just-compile []
  (cirru-sepal :paths ["cirru-demo"]
               :watch false))

(deftask watch-compile []
  (cirru-sepal :paths ["cirru-demo"]
               :watch true))
```

### License

MIT
