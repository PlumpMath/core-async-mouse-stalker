(ns mouse-stalker
  (:require-macros [cljs.core.async.macros :refer [go-loop]])
  (:require [cljs.core.async :refer [<! >! chan put!]]
            [dommy.core :as dommy]))

(def request-animation-frame
  (or js/requestAnimationFrame
      js/mozRequestAnimationFrame
      js/webkitRequestAnimationFrame))

(defn request-animation! [f]
  (letfn [(g [] (request-animation-frame g) (f))]
    (g)))

(def mouse-position
  (let [position (atom {:x 0, :y 0})]
    (dommy/listen! js/window
                   :mousemove
                   #(reset! position {:x (.-pageX %), :y (.-pageY %)}))
    (let [ch (chan)]
      (request-animation! #(put! ch @position))
      ch)))

(defn stalk [{to-x :x, to-y :y} {from-x :x, from-y :y}]
  (let [diff-x (- to-x from-x)
        diff-y (- to-y from-y)
        distance (Math/sqrt (+ (Math/pow diff-x 2) (Math/pow diff-y 2)))
        angle (Math/atan2 diff-y diff-x)]
    (if (< distance 2)
      {:x to-x, :y to-y}
      {:x (+ (* (Math/cos angle) (* distance 0.2)) from-x)
       :y (+ (* (Math/sin angle) (* distance 0.2)) from-y)})))

(defn append-star! [target]
  (let [element (dommy/create-element :div)]
    (dommy/set-text! element "★")
    (dommy/set-style! element
                      :color "#FFD700"
                      :font-size "20pt"
                      :position "absolute")
    (dommy/append! (dommy/sel1 :body) element)

    (let [ch (chan)]
      (go-loop [position {:x 0, :y 0}]
        (let [{:keys [x y] :as position} (stalk (<! target) position)]
          (dommy/set-px! element :left x, :top y)
          (>! ch position)
          (recur position)))
    ch)))

(let [last-star (reduce (partial append-star!) mouse-position (range 30))]
  (go-loop []
    (<! last-star)
    (recur)))
