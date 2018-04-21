(ns app.main
  (:require [app.vector :as vec]))

(def width 800)
(def height 400)
(def G 0.01) ;; universal gravitational constant


(defonce state
  (atom {:movers []
         :attractor {:location {:x (/ width 2) :y (/ height 2)}
                     :mass 50}}))

(declare calc-gravitation apply-direct-force)

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
    (let [x (+ 30 (* 50 i))
          y (+ 20 (* i 20))
          m (create x y 0 0 8 (js/random 20 40) 0 0 0)]
      m)))

(defn apply-a-vel [baton]
  (let [bacc (:a-acceleration baton)
        a-vel (+ (:a-velocity baton) bacc)
        angle (+ (:angle baton) a-vel)]
    (create-vec (:location baton)
                (:velocity baton)
                (:topspeed baton)
                (:mass baton)
                angle
                bacc
                a-vel)))

(defn apply-forces [mover]
  (let [force (calc-gravitation mover (:attractor @state))
        a (apply-direct-force (:mass mover) force)
        acc (/ (:x a) 10)
        avel (+ acc (:a-velocity mover))
        avel2 (js/constrain avel -0.1 0.1)
        angle (+ (:angle mover) avel2)
        v (vec/limit (vec/add (:velocity mover) a) (:topspeed mover))
        l (vec/add (:location mover) v)]
    (create-vec l v (:topspeed mover) (:mass mover) angle acc avel)))


(defn update-mover [mover]
  (apply-forces mover))

(defn apply-direct-force [mass force]
  (vec/div force mass))

(defn calc-gravitation [mover attractor]
  (let [force (vec/sub (:location attractor) (:location mover))
        distance (js/constrain (vec/mag force) 20 50)
        norm-force (vec/normalize force)
        strength (/ (* G (:mass attractor) (:mass mover))
                    (* distance distance))
        scaled-force (vec/mult force strength)]
    scaled-force))

(defn setup []
  (js/createCanvas width height)
  (swap! state assoc :movers (seed 30)))

(defn draw-mover [m]
  (let [location (:location m)
        mass (:mass m)
        angle (:angle m)
        cos_a (js/cos angle)
        sin_a (js/sin angle)
        tx (:x location)
        ty (:y location)]
    (js/stroke 40)
    (js/strokeWeight 3)
    (js/fill (- 255 (* 3 (js/ceil mass))))

    (js/translate tx ty)
    (js/rotate angle)
    (js/rect (- (/ mass 2)) (- (/ mass 2)) mass mass)
    (js/rotate (- angle))
    (js/translate (- tx) (- ty))))

(defn draw-attractor [attr]
  (js/fill 50)
  (js/ellipse (:x (:location attr)) (:y (:location attr)) (:mass attr) (:mass attr)))

(defn draw []
  (js/background 255)
  (let [list (:movers @state)
        attr (:attractor @state)]
    (swap! state assoc :movers (mapv update-mover list))

    (draw-attractor attr)
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
