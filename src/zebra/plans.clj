(ns zebra.plans
  (:import
    (com.stripe.model
      Plan)))

(defn plan->map
  [^Plan plan]
  {:id         (.getId plan)
   :active     (.getActive plan)
   :amount     (.getAmount plan)
   :created-at (.getCreated plan)
   :currency   (.getCurrency plan)
   :interval   (.getInterval plan)
   :product-id (.getProduct plan)
   :metadata   (.getMetadata plan)})
