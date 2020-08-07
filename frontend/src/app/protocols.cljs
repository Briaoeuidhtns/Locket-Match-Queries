(ns app.protocols)

;; I don't think this'll break anything
;; But I also don't know why it wasn't built in

(extend-type array
  IEmptyableCollection
    (-empty [_] #js []))

(extend-type object
  IEmptyableCollection
    (-empty [_] #js []))
