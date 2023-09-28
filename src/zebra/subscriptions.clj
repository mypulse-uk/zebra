(ns zebra.subscriptions
  (:refer-clojure :exclude [list])
  (:require
    [zebra.utils :refer [transform-params]])
  (:import
    (com.stripe.model
      Subscription
      StripeCollection)
    (com.stripe.net
      RequestOptions)
    (java.util
      Map)))

(defn subscription->map
  [^Subscription subscription]
  {:id                   (.getId subscription)
   :customer             (.getCustomer subscription)
   :cancelled-at         (.getCanceledAt subscription)
   :created-at           (.getCreated subscription)
   :current-period-start (.getCurrentPeriodStart subscription)
   :current-period-end   (.getCurrentPeriodEnd subscription)
   :plan                 (.getPlan subscription)
   :status               (.getStatus subscription)
   :metadata             (.getMetadata subscription)})

(defn subscriptions->map
  [^StripeCollection coll]
  {:has-more      (.getHasMore coll)
   :subscriptions (map subscription->map
                       (.getData coll))})

(defn create
  [params api-key]
  (subscription->map
    (Subscription/create ^Map (transform-params params)
                         (-> (RequestOptions/builder) (.setApiKey api-key) .build))))

(defn list
  ([params api-key]
   (subscriptions->map
     (Subscription/list ^Map (transform-params params)
                        (-> (RequestOptions/builder) (.setApiKey api-key) .build))))
  ([api-key]
   (list {} api-key)))
