(ns crossmint.core
  (:require [crossmint.megaverse :as megaverse]
            [clojure.string :as str]
            [clojure.set :as set]
            [diehard.core :as dh]
            [taoensso.timbre :as timbre]))

(defn generate-coordinates
  [max-x max-y]
  (for [x (range 0 max-x) y (range 0 max-y)]
    [x y]))

(defn goal-map-dimensions
  "Returns a vector of [rows columns] for a given goal map."
  [goal-map]
  [(count (first goal-map)) (count goal-map)])

(defn parse-astral-object
  ([s]
   (parse-astral-object nil s))
  ([[row col :as coords] s]
   (cond->> (str/split s #"_")
     :always      (map (comp keyword str/lower-case))
     :always      reverse
     :always      (zipmap [:type :trait])
     (seq coords) (merge {:row    row
                          :column col}))))

(defn astral-object->trait
  [{:keys [type]}]
  (case type
    :soloon :color
    :cometh :direction
    nil))

(defn rename-trait
  [astral-object]
  (set/rename-keys astral-object {:trait (astral-object->trait astral-object)} ))

(defn parse-goal-map
  [goal-map]
  (let [[rows columns] (goal-map-dimensions goal-map)
        coordinates (generate-coordinates rows columns)]
    (->> goal-map
         flatten
         (map parse-astral-object coordinates))))

(defn create-with-retry
  [candidate-id astral-object]
  (dh/with-retry {:retry-on Exception
                  :max-retries 5
                  :backoff-ms [1000 10000]
                  :on-retry (fn [v _] (timbre/info :retry {:values v :status :retrying}))
                  :on-success (fn [v _] (timbre/info :retry {:values v :status :success}))
                  :on-failure (fn [v _] (timbre/info :retry {:values v :status :failure}))}
    (megaverse/create (:type astral-object) (assoc astral-object :candidateId candidate-id))))

(defn get-astral-objects-to-create
  [candidate-id]
  (->> (megaverse/fetch-goal-map candidate-id)
       (parse-goal-map)
       (remove megaverse/space?)
       (map rename-trait)))

(defn populate-megaverse
  [candidate-id]
  (doseq [astral-object (get-astral-objects-to-create candidate-id)]
    (create-with-retry candidate-id astral-object)))

(defn -main
  [& [s]]
  (try
    (if-let [candidate-id (java.util.UUID/fromString s)]
      (populate-megaverse candidate-id)
      (println "\nMissing candidate id. \nCorrect usage: 'clojure -M -m crossmint.core <candidate-id>'"))
    (catch Exception _
      (println "\nInvalid candidate id provided. Should be an UUID."))))
