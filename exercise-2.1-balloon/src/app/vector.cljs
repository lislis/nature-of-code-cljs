(ns app.vector)

;; some vector helpers
;; state should look something like this
;; (defonce state
;;   (atom {:location (create 100 100)
;;          :velocity (create 2.5 3)}))

;; draw of bounce ball example
;; (defn draw []
;;   (let [v (add (:location @state) (:velocity @state))]
;;     (swap! state assoc :location v)
;;     (bounce-velocity state width height)
;;     (draw (:location @state))))

;; draw lines from center to mouse
;; (defn draw []
;; (let [v (vector/add (:location @state) (:velocity @state))
;;       m (vector/create js/mouseX js/mouseY)
;;       c (vector/create (/ width 2) (/ height 2))
;;       mouse (vector/sub m c)]
;;   (js/translate (/ width 2) (/ height 2))
;;   (js/line 0 0 (:x mouse) (:y mouse))))


(defn create
  "creates a vector"
  [x y]
  {:x x :y y})

(defn random2d
  "returns normalized random vector"
  []
  (let [x (js/random -5 5)
        y (js/random -5 5)]
    (normalize (create x y))))

(defn add
  "adds two vectors together"
  [vec1 vec2]
  (let [x (+ (:x vec1) (:x vec2))
        y (+ (:y vec1) (:y vec2))]
    (create x y)))

(defn sub
  "subtracts two vectors"
  [vec1 vec2]
  (let [x (- (:x vec1) (:x vec2))
        y (- (:y vec1) (:y vec2))]
    (create x y)))

(defn mult
  "multiplies vector by scalar"
  [vec scalar]
  (let [x (* (:x vec) scalar)
        y (* (:y vec) scalar)]
    (create x y)))

(defn div
  "divides vector by scalar"
  [vec scalar]
  (let [x (/ (:x vec) scalar)
        y (/ (:y vec) scalar)]
    (create x y)))

(defn mag
  "returns magnitude of a vector"
  [vec]
  (let [x (:x vec)
        y (:y vec)]
    (js/Math.sqrt (+ (* x x) (* y y)))))

(defn normalize
  "normalizes vector to 1"
  [vec]
  (let [m (mag vec)]
    (if (not (= m 0))
      (div vec m)
      vec)))

(defn limit
  "limit magnitude of vector, returns vector"
  [vec limit]
  (if (< (mag vec) limit)
    vec
    (mult (normalize vec) limit)))

(defn bounce-velocity [state width height]
  (if (or (> (:x (:location @state)) width) (< (:x (:location @state)) 0))
    (swap! state assoc :velocity {:x (* -1 (:x (:velocity @state))) :y (:y (:velocity @state))} ))

  (if (or (> (:y (:location @state)) height) (< (:y (:location @state)) 0))
    (swap! state assoc :velocity {:y (* -1 (:y (:velocity @state))) :x (:x (:velocity @state))} )))

(defn draw [vector]
  (js/background 180 220 140)
  (js/stroke 0)
  (js/fill 200 50 100)
  (js/ellipse (:x vector) (:y vector) 20 20))
