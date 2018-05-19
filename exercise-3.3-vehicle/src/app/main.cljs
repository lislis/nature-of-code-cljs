(ns app.main
  (:require [app.vector :as vec]))

(def width 800)
(def height 400)

(defonce state
  (atom {:movers []}))

(defn create-vec [l v topspeed mass angle aacc avel velocity-mag]
  {:location l
   :velocity v
   :acceleration {:x 0 :y 0}
   :topspeed topspeed
   :mass mass
   :angle angle
   :a-acceleration aacc
   :a-velocity avel
   :velocity-mag velocity-mag})

(defn create [x y vx vy topspeed mass angle aacc avel velocity-mag]
  (create-vec
   (vec/create x y)
   (vec/create vx vy)
   topspeed
   mass
   angle
   aacc
   avel
   velocity-mag))

(defn seed [num]
  (for [i (range num)]
    (let [x (/ width 2)
          y (/ height 2)
          m (create x y 0 0 8 (js/random 20 40) 0 0 0 0)]
      m)))

(defn apply-direct-force [mass force]
  (vec/div force mass))

(defn apply-forces [mover]
  (let [aacc (:a-acceleration mover)
        avel (+ aacc (:a-velocity mover))
        angle (+ avel (:angle mover))

        vmag (:velocity-mag mover)

        vx (* (js/cos angle) vmag)
        vy (* (js/sin angle) vmag)

        v (vec/create vx vy)
        l (vec/add (:location mover) v)]
    (js/console.log aacc)
    (create-vec l v (:topspeed mover) (:mass mover) angle 0 0 vmag)))

(defn update-mover [mover]
  (apply-forces mover))

(defn setup []
  (js/createCanvas width height)
  (swap! state assoc :movers (seed 1)))

(defn draw-car []
  (let [h 50
        w 30
        w2 (/ w 2)
        th 14
        tw 10]
    (js/rect (- w2) (- w2) w h)
    (js/fill 40)
    (js/ellipse w2 w tw th)
    (js/ellipse w2 (- w2 15) tw th)
    (js/ellipse (- w2) w tw th)
    (js/ellipse (- w2) (- w2 15) tw th)
    (js/rect (- w2) (- w2) w 5)))

(defn draw-mover [m]
  (let [location (:location m)
        velocity (:velocity m)
        mass (:mass m)
        angle (:angle m)
        tx (:x location)
        ty (:y location)]
    (js/stroke 40)
    (js/strokeWeight 3)
    (js/fill (- 255 (* 3 (js/ceil mass))))
    (js/translate tx ty)
    (js/rotate (+ angle (js/radians 90)))
    (draw-car)

    (js/resetMatrix)))

(defn draw []
  (js/background 255)
  (let [list (:movers @state)]
    (swap! state assoc :movers (mapv update-mover list))
    (dorun
     (for [m list]
       (draw-mover m)))))

(defn steer-left [mover]
  (let [a (:a-acceleration mover)
        new-a (- a 0.2)]
    (assoc mover :a-acceleration new-a)))

(defn steer-right [mover]
  (let [a (:a-acceleration mover)
        new-a (+ a 0.2)]
    (assoc mover :a-acceleration new-a)))

(defn accelerate [mover]
  (let [a (:velocity-mag mover)
        new-a (+ a 0.2)]
    (assoc mover :velocity-mag new-a)))

(defn decelerate [mover]
  (let [a (:velocity-mag mover)
        new-a (- a 0.2)]
    (assoc mover :velocity-mag new-a)))

(defn steer [direction]
  (swap! state assoc :movers (mapv direction (:movers @state))))

(defn keypressed []
  (let [up 38
        down 40
        left 37
        right 39]
    (condp = js/keyCode
      left (steer steer-left)
      right (steer steer-right)
      up (steer accelerate)
      down (steer decelerate)
      (js/console.log "not configured"))))

;; start stop pattern as described in
;; https://github.com/thheller/shadow-cljs/wiki/ClojureScript-for-the-browser
(defn start []
  (doto js/window
    (aset "setup" setup)
    (aset "keyPressed" keypressed)
    (aset "draw" draw))
  (js/console.log "START"))

(defn stop []
  (js/clear)
  (js/console.log "STOP"))

(defn ^:export init []
  (js/console.log "INIT")
  (start))
