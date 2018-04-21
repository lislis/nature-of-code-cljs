(ns app.main
  (:require [app.vector :as vec]))

(def width 800)
(def height 400)

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

;; (defn apply-direct-force [mass force]
;;   (vec/div force mass))

;; (defn apply-forces [mover]
;;   (let [force (calc-gravitation mover (:attractor @state))
;;         a (apply-direct-force (:mass mover) force)
;;         v (vec/limit (vec/add (:velocity mover) a) (:topspeed mover))
;;         l (vec/add (:location mover) v)]
;;     (create-vec l v
;;                 (:topspeed mover)
;;                 (:mass mover)
;;                 (:angle mover)
;;                 (:a-acceleration mover)
;;                 (:a-vel mover))))

(defn apply-a-vel [baton]
  (let [bacc (:a-acceleration baton)
        a-vel (+ (:a-velocity baton) bacc)
        angle (+ (:angle baton) a-vel)]
    ;;(js/console.log a-vel)
    (create-vec (:location baton)
                (:velocity baton)
                (:topspeed baton)
                (:mass baton)
                angle
                bacc
                a-vel)))

(defn update-baton [baton]
  (let [b (apply-a-vel baton)]
    b))

(defonce state
  (atom {:baton (create 0 0 0 0 5 20 0 0.001 0)}))

(defn setup []
  (js/createCanvas width height))

(defn draw []
  (js/background 255)
  (let [baton (:baton @state)]
    (swap! state assoc :baton (update-baton baton))

    (js/translate (/ width 2) (/ height 2))
    (js/rotate (:angle baton))
    (js/stroke 50)
    (js/line -50 0 50 0)
    (js/ellipse -50 0 20 20)
    (js/ellipse 50 0 20 20)))

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
