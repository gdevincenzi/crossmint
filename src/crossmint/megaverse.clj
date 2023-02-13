(ns crossmint.megaverse
  (:require [crossmint.lib.http :as http]))

;; Utils
(def api-root "https://challenge.crossmint.io/api")

(def astral-object-types #{:polyanet :soloon :cometh :space})
(def soloon-colors #{:blue :red :purple :white})
(def cometh-directions #{:up :down :left :right})

(defn- is?
  [astral-object {:keys [type]}]
  (= astral-object type))

(def space? (partial is? :space))
(def polyanet? (partial is? :polyanet))
(def soloon? (partial is? :soloon))
(def cometh? (partial is? :cometh))

(defn- type->endpoint
  [astral-object-type]
  {:pre [(contains? (disj astral-object-types :space) astral-object-type)]}
  (str api-root "/" (name astral-object-type) "s"))

(defn type->keys
  [astral-object-type]
  (cond-> [:candidateId :row :column]
    (= astral-object-type :soloon) (conj :color)
    (= astral-object-type :cometh) (conj :direction)))

;; API
(defn fetch-goal-map
  [candidate-id]
  (->> (http/get (str api-root "/map/" candidate-id "/goal"))
       :body
       :goal))

(defn create
  [astral-object-type params]
  (http/post (type->endpoint astral-object-type)
             {:form-params (select-keys params (type->keys astral-object-type))}))

(defn destroy
  [astral-object-type {:keys [row column candidate-id]}]
  (http/delete (type->endpoint astral-object-type) {:form-params {:candidateId candidate-id
                                                                  :row         row
                                                                  :column      column}}))
