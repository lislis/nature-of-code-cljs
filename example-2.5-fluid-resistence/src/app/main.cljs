(ns app.main
  (:require [app.vector :as vec]))

(def width 800)
(def height 400)
(def c 0.1) ;; coefficient of friction
(def normal 1) ;; simplified normal force N

(defonce state
  (atom {:movers []
         :forces {:wind {:x 0.0 :y 0.0}
                  :gravity {:x 0.0 :y 0.18}}
         :liquid {:x 0
                  :y (/ height 2)
                  :w width
                  :h (/ height 2)
                  :c 2.0}}))

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
    (let [m (create (+ 30 (* 50 i)) 50 0 0 0 0 10 (js/random 20 70))]
      m)))

(defn bounce-wall [l w h]
  (let [x (cond (> (:x l) w) w
                (< (:x l) 0) 0
                :else (:x l))
        y (if (> (:y l) h)
            h
            (:y l))]
    (vec/create x y)))

(defn bounce-vel [v l w h]
  (let [x (cond
            (> (:x l) w) (* -1 (:x v))
            (< (:x l) 0) (* -1 (:x v))
            :else (:x v))
        y (if (> (:y l) h)
            (* -1 (:y v))
            (:y v))]
    (vec/create x (:y v))))

(defn apply-direct-force [mass force]
  (vec/div force mass))

(defn apply-relative-force [mass force]
  (let [rel-force (* (:y force) mass)
        calc-force (vec/create (:x force) rel-force)]
    (vec/div calc-force mass)))

(defn inside? [mover liquid]
  (let [m (:location mover)
        l liquid]
    (if (and (> (:x m) (:x l))
             (< (:x m) (+ (:x l) (:w l)))
             (> (:y m) (:y l))
             (< (:y m) (+ (:y l) (:h l))))
      true
      false)))

(defn apply-drag [mass vel]
  (let [speed (vec/mag vel)
        drag-mag (* (:c (:liquid @state)) speed speed)
        drag (-> vel
                 (vec/mult -1)
                 (vec/normalize)
                 (vec/mult drag-mag))]
    (vec/div drag mass)))

(defn accumulate-forces [mover force-list]
  (let [mass (:mass mover)
        vel (:velocity mover)
        wind (:wind force-list)
        f1 (apply-direct-force mass wind)
        gravity (:gravity force-list)
        f2 (apply-relative-force mass gravity)
        ;; friction-mag (* c normal)
        ;; friction (-> vel
        ;;              (vec/mult -1)
        ;;              (vec/normalize)
        ;;              (vec/mult friction-mag))
        drag (apply-drag mass vel)]
    (if (inside? mover (:liquid @state))
      (vec/add (vec/add f1 f2) drag)
      (vec/add f1 f2))))

(defn apply-forces [mover force-list]
  (let [f (accumulate-forces mover force-list)
        a f
        v1 (vec/limit (vec/add (:velocity mover) a) (:topspeed mover))
        l1 (vec/add (:location mover) v1)
        v (bounce-vel v1 l1 width height)
        l (bounce-wall l1 width height)]
    (create-vec l v (vec/create 0 0) (:topspeed mover) (:mass mover))))

(defn update-mover [mover]
  (let [force-list (:forces @state)]
    (apply-forces mover force-list)))

(defn setup []
  (js/createCanvas width height)
  (swap! state assoc :movers (seed 15)))

(defn draw-liquid []
  (let [liq (:liquid @state)]
    (js/noStroke)
    (js/fill 80)
    (js/rect (:x liq) (:y liq) (:w liq) (:h liq))))

(defn draw []
  (js/background 255)
  (let [list (:movers @state)]
    (swap! state assoc :movers (mapv update-mover list))

    (draw-liquid)
    (dorun
     (for [m list]
       (let [location (:location m)
             mass (:mass m)]
         (js/stroke 180)
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
