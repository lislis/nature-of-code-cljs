(ns app.main
  (:require [app.vector :as vec]))

(def width 800)
(def height 400)
(def G 0.2) ;; universal gravitational constant

(defonce state
  (atom {:movers []
         :attractor {:location {:x (/ width 2) :y (/ height 2)}
                     :mass 50}}))

(defn create-vec [l v a topspeed mass]
  {:location l
   :velocity v
   :acceleration a
   :topspeed topspeed
   :mass mass})

(defn create [x y vx vy ax ay topspeed mass]
  (create-vec
   (vec/create x y)
   (vec/create vx vy)
   (vec/create ax ay)
   topspeed
   mass))

(defn seed [num]
  (for [i (range num)]
    (let [m (create (+ 30 (* 50 i)) (+ 20 (* i 20)) 0 0 0 0 8 (js/random 20 40))]
      m)))

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

(defn apply-forces [mover]
  (let [force (calc-gravitation mover (:attractor @state))
        a (apply-direct-force (:mass mover) force)
        v (vec/limit (vec/add (:velocity mover) a) (:topspeed mover))
        l (vec/add (:location mover) v)]
    (create-vec l v (vec/create 0 0) (:topspeed mover) (:mass mover))))

(defn update-mover [mover]
  (apply-forces mover))

(defn setup []
  (js/createCanvas width height)
  (swap! state assoc :movers (seed 10)))

(defn draw-attractor [attr]
  (js/fill 50)
  (js/ellipse (:x (:location attr)) (:y (:location attr)) (:mass attr) (:mass attr)))

(defn draw-mover [m]
  (let [location (:location m)
        mass (:mass m)]
    (js/stroke 180)
    (js/fill (- 255 (* 3 (js/ceil mass))))
    (js/ellipse (:x location) (:y location) mass mass)))

(defn draw []
  (js/background 255)
  (let [list (:movers @state)
        attr (:attractor @state)]
    (swap! state assoc :movers (mapv update-mover list))

    (draw-attractor attr)
    (dorun
     (for [m list]
       (draw-mover m)))))


(defn mousepressed []
  (swap! state assoc-in [:attractor :location] {:x js/mouseX :y js/mouseY}))

;; start stop pattern as described in
;; https://github.com/thheller/shadow-cljs/wiki/ClojureScript-for-the-browser
(defn start []
  (doto js/window
    (aset "setup" setup)
    (aset "mouseDragged" mousepressed)
    (aset "draw" draw))
  (js/console.log "START"))

(defn stop []
  (js/clear)
  (js/console.log "STOP"))

(defn ^:export init []
  (js/console.log "INIT")
  (start))
