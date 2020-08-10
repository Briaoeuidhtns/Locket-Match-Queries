(ns locket-match-queries.scalar
  "Parsers for custom graphql scalar types.

  Eventually this should be on the client side too,
  but doesn't look like apollo completely supports it yet."
  #?(:cljs (:require-macros locket-match-queries.scalar))
  (:require
   [slingshot.slingshot :refer [throw+]]))

(def definitions
  "The map of all scalars defined via `defscalar`.
  Suitable for use as the value of `:scalars` in a lacinia schema"
  {})

;; TODO put some of this in a fn
(defmacro defscalar
  "Define a new scalar with conversion functions.
  ```clojure
  (defscalar Scalar
  \"optional docstring\"
  ;; A fn form or a symbol
    ([to-parse] (parse to-parse))
  ;; Optional serialize, defaults to same as parse
    ([to-serialize] (serialize to-serialize)))
  ```"
  ;; either missing serialize or docstring
  ([n parse-or-doc serialize-or-both]
   (let [[doc parse serialize]
         (if (string? parse-or-doc)
           ;; TEMP duplicating fn defs
           [parse-or-doc serialize-or-both serialize-or-both]
           ["" parse-or-doc serialize-or-both])]
     `(defscalar ~n ~doc ~parse ~serialize)))
  ([n doc parse serialize]
   (let [transf-scalar {:parse (if (seq? parse) `(fn ~'parse ~@parse) parse)
                        :serialize (if (seq? serialize)
                                     `(fn ~'serialize ~@serialize)
                                     serialize)}]
     `(do (def ~n ~doc ~transf-scalar)
          (alter-var-root #'definitions
                          ~assoc
                          (~keyword '~n)
                          (~assoc ~transf-scalar :description ~doc))))))

(def int53-max
  "Maximum int that can be losslessly stored in an IEEE 754 double"
  (dec (bit-shift-left 1 53)))
(def int53-min
  "Minimum int that can be losslessly stored in an IEEE 754 double"
  (- int53-max))
(defscalar
  Int53
  "A scalar type covering the range of integers that can safely be stored in an IEEE 754 double. Silently converts non-int numbers to ints"
  ([v]
   (cond (not (number? v)) (throw+ {:type ::not-a-number :value v})
         (not (<= int53-min v int53-max)) (throw+ {:type ::out-of-range
                                                   :value v})
         ;; Ends up as a json `number` so fine as long as it's in range
         :else v)))

;; TODO instants
