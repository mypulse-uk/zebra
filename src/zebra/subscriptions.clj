(ns zebra.subscriptions
  (:refer-clojure :exclude [list])
  (:require
    [zebra.payment-intents :refer [payment-intent->map]]
    [zebra.plans :refer [plan->map]]
    [zebra.utils :refer [transform-params]])
  (:import
    (com.stripe.model
      StripeCollection
      Subscription)
    (com.stripe.net
      RequestOptions)
    (java.util
      Map)))

(defn subscription->map
  [^Subscription subscription]
  (merge {:id                   (.getId subscription)
          :customer             (.getCustomer subscription)
          :cancelled-at         (.getCanceledAt subscription)
          :created-at           (.getCreated subscription)
          :current-period-start (.getCurrentPeriodStart subscription)
          :current-period-end   (.getCurrentPeriodEnd subscription)
          :plan                 (plan->map (.getPlan subscription))
          :status               (.getStatus subscription)
          :metadata             (.getMetadata subscription)}
         (when-let [latest-invoice (.getLatestInvoiceObject subscription)]
           {:latest-invoice {:amount-due     (.getAmountDue latest-invoice)
                             :amount-paid    (.getAmountPaid latest-invoice)
                             :attempt-count  (.getAttemptCount latest-invoice)
                             :billing-reason (.getBillingReason latest-invoice)
                             :charge         (.getCharge latest-invoice)
                             :created-at     (.getCreated latest-invoice)
                             :id             (.getId latest-invoice)
                             :payment-intent (payment-intent->map
                                               (.getPaymentIntentObject latest-invoice))}})))

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

(defn retrieve
  [id api-key]
  (subscription->map
    (Subscription/retrieve id
                           (-> (RequestOptions/builder) (.setApiKey api-key) .build))))

(defn list
  ([params api-key]
   (subscriptions->map
     (Subscription/list ^Map (transform-params params)
                        (-> (RequestOptions/builder) (.setApiKey api-key) .build))))
  ([api-key]
   (list {} api-key)))
