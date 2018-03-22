(ns app.main
  (:require [app.vector :as vec]))

(defn create-vec [l v a topspeed]
  {:location l
   :velocity v
   :acceleration a
   :topspeed topspeed})

(defn create [x y vx vy ax ay topspeed]
  (create-vec
   (vec/create x y)
   (vec/create vx vy)
   (vec/create ax ay)
   topspeed))

(defn seed [num]
  (for [i (range num)]
    (let [m (create (+ 100 (* 100 i)) (js/random 100 150) 0 0 0 0 2)]
      m)))

(defn wrap-edges [vec width]
  (let [x (cond (> (:x vec) width) 20
                (< (:x vec) 20) width
                :else (:x vec))]
    (vec/create x (:y vec))))

(defn apply-force [mover force]
  (let [a (if (> (:y (:location mover)) 35) ; half the balloon height
            force
            (vec/add force {:x 0 :y 0.3})) ; force of the ceiling pushing back
        v (vec/limit (vec/add (:velocity mover) a) (:topspeed mover))
        l (wrap-edges (vec/add (:location mover) v) width)]
    (create-vec l v (vec/create 0 0) (:topspeed mover))))

(defn accumulate-forces [force-list]
  (let [raw-forces (vals force-list)
        sum-force (reduce vec/add raw-forces)]
    sum-force))

(defn update-mover [mover]
  (let [force-list (:forces @state)
        forces (accumulate-forces force-list)]
    (apply-force mover forces)))

(defn perlin-wind [wind]
  (let [xoff (+ (:xoff wind) 0.01)
        x (js/map (js/noise xoff) 0 1 -0.02 0.02)]
    {:x x :y 0 :xoff xoff}))

(def width 600)
(def height 400)

(defonce state
  (atom {:movers []
         :forces {:wind {:x 0.0 :y 0.0 :xoff 100000}
                  :helium {:x 0.0 :y -0.008}}}))

(defn setup []
  (js/createCanvas width height)
  (swap! state assoc :movers (seed 6)))

(defn draw []
  (js/background 255)
  (let [list (:movers @state)
        wind (:wind (:forces @state))]
    (swap! state assoc :movers (mapv update-mover list))
    (swap! state assoc-in [:forces :wind] (perlin-wind wind))
    (dorun
     (for [m list]
       (let [location (:location m)]
         (js/stroke 0)
         (js/fill 120 180 50)
         (js/ellipse (:x location) (:y location) 50 70))))
    ))


;; start stop pattern as described in
;; https://github.com/thheller/shadow-cljs/wiki/ClojureScript-for-the-browser
(defn start []
  (doto js/window
    (aset "setup" setup)
    (aset "draw" draw))
  (js/console.log "START"))

(defn stop []
  (js/clear)
  (js/console.log "STOP"))

(defn ^:export init []
  (js/console.log "INIT")
  (start))
