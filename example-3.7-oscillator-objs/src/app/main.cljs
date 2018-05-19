(ns app.main
  (:require [app.oscillator :as o]))

(def width 800)
(def height 400)

(defn spawn-osc []
  (o/create (js/random -0.05 0.05) (js/random -0.05 0.05)
            (js/random (/ width 2)) (js/random (/ height 2))))

(defn seed [num]
  (for [i (range num)]
    (let [m (spawn-osc)]
      m)))

(defonce state
  (atom {:osc []}))

(defn setup []
  (js/createCanvas width height)
  (swap! state assoc :osc (seed 12)))

(defn draw []
  (js/background 255)
  (let [osc  (:osc @state)]
    (swap! state assoc :osc (mapv o/oscillate osc))
    (dorun
     (for [m osc]
       (let [angle (:angle m)
             ampl (:amplitude m)
             x (* (js/sin (:x angle)) (:x ampl))
             y (* (js/sin (:y angle)) (:y ampl))]
         (js/translate (/ width 2) (/ height 2))
         (js/noStroke)
         (js/fill 60 60 80)
         (js/ellipse x y  20 20)
         (js/stroke 60 60 120)
         (js/line 0 0 x y)
         (js/resetMatrix))))))

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
;;
(defn ^:export init []
  (js/console.log "INIT")
  (start))
