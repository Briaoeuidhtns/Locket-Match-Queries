(ns app.helix
  (:require
   [helix.core])
  #?(:cljs (:require-macros app.helix)))


(defmacro defnc
  [type params & body]
  (let [[docstring params body] (if (string? params)
                                  [params (first body) (rest body)]
                                  [nil params body])
        ;; whether an opts map was passed in
        opts? (map? (first body))
        opts (if opts? (first body) {})
        body (if opts? (rest body) body)
        ;; feature flags to enable by default
        default-opts {:helix/features {:fast-refresh true
                                       :check-invalid-hooks-usage true}}]
    `(helix.core/defnc ~type
                       ~@(when docstring [docstring])
                       ~params
                       ;; we use `merge` here to allow indidivual consumers to
                       ;; override feature flags in special cases
                       ~(merge default-opts opts)
                       ~@body)))
