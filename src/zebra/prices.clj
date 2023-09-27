(ns zebra.prices
  (:require
    [zebra.utils :refer [transform-params]])
  (:import
    (com.stripe.model
      Price)
    (com.stripe.net
      RequestOptions)
    (java.util
      Map)))

(defn price->map
  [^Price price]
  {:id          (.getId price)
   :currency    (.getCurrency price)
   :unit-amount (.getUnitAmount price)
   :product     (.getProduct price)
   :metadata    (.getMetadata price)})

(defn create
  [params api-key]
  (price->map
    (Price/create ^Map (transform-params params)
                  (-> (RequestOptions/builder) (.setApiKey api-key) .build))))

(defn retrieve
  [id api-key]
  (price->map
    (Price/retrieve id
                    (-> (RequestOptions/builder) (.setApiKey api-key) .build))))
