(ns crossmint.lib.log
  (:require [taoensso.timbre :as timbre]))

(defn logged
  [name f]
  (fn [& args]
    (timbre/info name :request args)
    (let [response (apply f args)]
      (timbre/info name :response response)
      response)))
