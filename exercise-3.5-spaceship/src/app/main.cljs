(ns app.main)

(def width 800)
(def height 400)

(defonce state
  (atom {:x (/ width 2)
         :y (/ height 2)
         :dir -1.5707964
         :speed 0}))

(defn setup []
  (js/createCanvas width height))

(defn shape-spaceship []
  (let [x 0
        y -15
        left-x (- x 15)
        left-y (+ y 30)
        right-x (+ x 15)]
    (js/stroke 60)
    (js/strokeWeight 2)
    (js/fill 150)
    (js/triangle x y left-x left-y right-x left-y)
    (if (= (js/keyIsDown 90) true)
      (js/fill 200 30 30))
    (js/rect (+ left-x 4) left-y 6 6)
    (js/rect (- right-x 11) left-y 6 6)))

(defn draw-spaceship [thing]
  (let [dir (:dir thing)
        speed (:speed thing)
        x (:x thing)
        y (:y thing)]
    (js/translate x y)
    (js/rotate (+ dir (js/radians 90)))
    (shape-spaceship)
    (js/resetMatrix)))

(defn rot [vec]
  (let [fnc (first vec)
        amount (last vec)
        angle-old (:dir @state)
        angle (fnc angle-old amount)]
    (swap! state assoc :dir angle)))

(defn thrusters []
  (let [speed (:speed @state)
        new-speed (+ speed 0.2)]
    (swap! state assoc :speed new-speed)))

(defn update-location []
  (let [x (:x @state)
        y (:y @state)
        speed (:speed @state)
        dir (:dir @state)
        vx (* (js/cos dir) speed)
        vy (* (js/sin dir) speed)
        lx (+ x vx)
        ly (+ y vy)
        next-speed (if (> speed 0)
                     (- speed 0.03)
                     0)]
    {:x lx :y ly :speed next-speed}))

(defn draw []
  (js/background 255)
  (let []
    (swap! state merge @state (update-location))
    (draw-spaceship @state)
    (cond
      (= (js/keyIsDown js/LEFT_ARROW) true) (rot [- 0.05])
      (= (js/keyIsDown js/RIGHT_ARROW) true) (rot [+ 0.05])
      (= (js/keyIsDown 90) true) (thrusters))))

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
