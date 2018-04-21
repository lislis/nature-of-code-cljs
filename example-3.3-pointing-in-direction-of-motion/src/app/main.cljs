(ns app.main
  (:require [app.vector :as vec]))

(def width 800)
(def height 400)

(defonce state
  (atom {:movers []}))

(defn create-vec [l v topspeed mass angle aacc avel]
  {:location l
   :velocity v
   :acceleration {:x 0 :y 0}
   :topspeed topspeed
   :mass mass
   :angle angle
   :a-acceleration aacc
   :a-velocity avel})

(defn create [x y vx vy topspeed mass angle aacc avel]
  (create-vec
   (vec/create x y)
   (vec/create vx vy)
   topspeed
   mass
   angle
   aacc
   avel))

(defn seed [num]
  (for [i (range num)]
    (let [x (js/random 0 width)
          y (js/random 0 height)
          m (create x y 0 0 8 (js/random 20 40) 0 0 0)]
      m)))

(defn apply-direct-force [mass force]
  (vec/div force mass))

(defn apply-forces [mover]
  (let [mouse-l (vec/create js/mouseX js/mouseY)
        move-l (:location mover)
        move-v (:velocity mover)
        dir (vec/sub mouse-l move-l)
        force (vec/mult (vec/normalize dir) 0.5)
        a force
        aacc (/ (:x a) 10)
        avel (+ aacc (:a-velocity mover))
        avel2 (js/constrain avel -0.1 0.1)
        angle (js/atan2 (:y move-v) (:x move-v))
        v (vec/limit (vec/add (:velocity mover) a) (:topspeed mover))
        l (vec/add (:location mover) v)]
    (create-vec l v (:topspeed mover) (:mass mover) angle aacc avel)))

(defn update-mover [mover]
  (apply-forces mover))

(defn setup []
  (js/createCanvas width height)
  (swap! state assoc :movers (seed 1)))

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
    (js/rotate angle)
    (js/rect (- (/ 30 2)) (- (/ 10 2)) 30 10)
    (js/resetMatrix)))

(defn draw []
  (js/background 255)
  (let [list (:movers @state)]
    (swap! state assoc :movers (mapv update-mover list))

    (dorun
     (for [m list]
       (draw-mover m)))))

;; start stop pattern as described in
;; https://github.com/thheller/shadow-cljs/wiki/ClojureScript-for-the-browser
(defn start []
  (doto js/window
    (aset "setup" setup)
    ;;(aset "mouseDragged" mousepressed)
    (aset "draw" draw))
  (js/console.log "START"))

(defn stop []
  (js/clear)
  (js/console.log "STOP"))

(defn ^:export init []
  (js/console.log "INIT")
  (start))
