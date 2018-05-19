(ns app.oscillator
  (:require [app.vector :as v]))

(defn create [vx vy ax ay]
  {:angle (v/create 0 0)
   :velocity (v/create vx vy)
   :amplitude (v/create ax ay)})

(defn oscillate [osc]
  (let [angle (:angle osc)
        vel (:velocity osc)
        new-angle (v/add angle vel)]
    (assoc osc :angle new-angle)))
