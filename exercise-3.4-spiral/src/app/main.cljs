(ns app.main)

(def width 800)
(def height 400)

(defonce state
  (atom {:theta 0
         :r 0}))

(defn setup []
  (js/createCanvas width height))

(defn draw-thing [thing]
  (let [theta (:theta thing)
        r (:r thing)
        calc-x (* r (js/cos theta))
        calc-y (* r (js/sin theta))
        center-x (/ width 2)
        center-y (/ height 2)
        x (+ calc-x center-x)
        y (+ calc-y center-y)]
    (js/stroke 100)
    (js/strokeWeight 3)
    (js/fill 100)
    ;(js/line center-x center-y x y)
    (js/ellipse x y 10 10)))

(defn draw []
  ;(js/background 255)
  (let [theta (+ (:theta @state) 0.01)
        r (+ (:r @state) 0.1)]
    (reset! state {:theta theta :r r})
    (draw-thing @state)))

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
