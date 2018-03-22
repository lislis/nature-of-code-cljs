(ns app.main
  (:require [app.vector :as vec]))


(def width 800)
(def height 400)

(defonce state
  (atom {:movers []
         :forces {:wind {:x 0.0 :y 0.0}
                  :gravity {:x 0.0 :y 0.2}}}))


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
    (let [m (create (+ 100 (* 50 i)) 50 0 0 0 0 3 (js/randomGaussian 30 20))]
      m)))

(defn apply-force [mover force]
  (let [mass (:mass mover)
        f (vec/div force mass)
        a f
        v1 (vec/limit (vec/add (:velocity mover) a) (:topspeed mover))
        l1 (vec/add (:location mover) v1)
        v (cond
              (> (:x l1) width) (vec/create (* -1 (:x v1)) (:y v1))
            (< (:x l1) 0) (vec/create (* -1 (:x v1)) (:y v1))
            (> (:y l1) height) (vec/create (:x v1) (* -1 (:y v1)))
            :else v1)
        l (cond (> (:x l1) width) (vec/create width (:y l1))
            (< (:x l1) 0) (vec/create 0 (:y l1))
            (> (:y l1) height) (vec/create (:x l1) height)
            :else l1)]
    (create-vec l v (vec/create 0 0) (:topspeed mover) (:mass mover))))

(defn accumulate-forces [force-list]
  (let [raw-forces (vals force-list)
        sum-force (reduce vec/add raw-forces)]
    sum-force))

(defn update-mover [mover]
  (let [force-list (:forces @state)
        forces (accumulate-forces force-list)]
    (apply-force mover forces)))

(defn setup []
  (js/createCanvas width height)
  (swap! state assoc :movers (seed 20)))

(defn draw []
  (js/background 255)
  (let [list (:movers @state)]
    (swap! state assoc :movers (mapv update-mover list))

    (dorun
     (for [m list]
       (let [location (:location m)
             mass (:mass m)]
         (js/fill (- 255 (* 3 (js/ceil mass))))
         (js/ellipse (:x location) (:y location) mass mass))))
    ))

(defn blow-left []
  (let [f (:forces @state)
        wind (:wind f)
        new-wind (vec/add wind (vec/create 0.05 0))]
    (swap! state assoc-in [:forces :wind]  new-wind)
    new-wind))

(defn blow-right []
  (let [f (:forces @state)
        wind (:wind f)
        grav (:gravity f)
        new-wind (vec/add wind (vec/create -0.05 0))]
    (swap! state assoc-in [:forces :wind] new-wind)
    new-wind))

(defn keypressed []
  (let [left 37
        right 39
        up 38
        down 40]
    (condp = js/keyCode
      left (blow-left)
      right (blow-right)
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
