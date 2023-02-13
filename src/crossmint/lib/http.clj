(ns crossmint.lib.http
  (:refer-clojure :exclude [get])
  (:require [clj-http.client :as client]
            [crossmint.lib.log :as log]))

(defn -get [url & [opts]]
  (client/get url (merge {:as :json} opts)))

(defn -post [url & [opts]]
  (client/post url (merge {:content-type :json} opts)))

(defn -delete [url & [opts]]
  (client/delete url opts))

(def get  (log/logged :get -get))
(def post (log/logged :post -post))
(def delete (log/logged :delete -delete))
