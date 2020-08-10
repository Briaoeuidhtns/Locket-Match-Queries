(ns locket-match-queries.scalar-test
  (:require
   [locket-match-queries.scalar :as sut]
   [slingshot.test]
   [clojure.test :as t]))

;; FAIL These should be the same fn
(t/deftest ^:kaocha/pending int53-shares-parse-and-serialize
  (let [{:keys [parse serialize]} sut/Int53] (t/is (= parse serialize))))

(t/deftest can-transform-int53
  (let [{:keys [serialize]} sut/Int53
        gt-int-max (inc Integer/MAX_VALUE)]
    (t/testing "parse/serialize int53"
               (t/testing "success"
                          (t/is (= 1 (serialize 1)))
                          (t/is (= gt-int-max (serialize gt-int-max))))
               (t/testing "failure"
                          (t/is (thrown+? [:type ::sut/out-of-range]
                                          (serialize (bit-shift-left 1 54))))
                          (t/is (thrown+? [:type ::sut/not-a-number]
                                          (serialize "not an integer")))))))
