(ns app.main)

(def width 800)
(def height 400)
(def amplitude 150)
(def period 120)

(defonce state
  (atom {}))

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
    (js/stroke 40)
    (js/strokeWeight 3)
    (js/fill 100)
    (js/line center-x center-y x y)
    (js/ellipse x y 30 30)))

(defn draw []
  (js/background 255)
  (let [y (* amplitude (js/cos (* js/TWO_PI (/ js/frameCount period))))]
    (js/stroke 60)
    (js/strokeWeight 3)
    (js/fill 160)
    (js/translate (/ width 2) (/ height 3))
    (js/line 0 0 0 y)
    (js/ellipse 0 y 30 30)
    (js/resetMatrix)))

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
