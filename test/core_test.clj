(ns core-test
  (:require  [clojure.test :refer :all]
             [crossmint.core :as crossmint]
             [crossmint.megaverse :as megaverse]))


;; Utils
(defn generate-soloons
  []
  (for [color ["BLUE" "RED" "WHITE" "PURPLE"]]
    (str color "_SOLOON")))

(defn generate-comeths
  []
  (for [direction ["UP" "DOWN" "LEFT" "RIGHT"]]
    (str direction "_COMETH")))

(def raw-astral-objects (concat (generate-comeths) (generate-soloons) ["POLYANET" "SPACE"]))

;; Tests
(deftest astral-object-parsing
  (testing "Parses a collection of raw Astral Objects correctly"
    (let [parsed-objects (->> raw-astral-objects
                              (map crossmint/parse-astral-object))]
      (is (every? megaverse/astral-object-types (map :type parsed-objects)))

      (is (every? megaverse/soloon-colors (->> parsed-objects
                                               (filter megaverse/soloon?)
                                               (map :trait))))

      (is (every? megaverse/cometh-directions (->> parsed-objects
                                                   (filter megaverse/cometh?)
                                                   (map :trait)))))))
