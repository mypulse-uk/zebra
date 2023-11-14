(ns zebra.invoice-items
  (:require
    [zebra.utils :refer [transform-params]])
  (:import
    (com.stripe.model
      InvoiceItem)
    (com.stripe.net
      RequestOptions)
    (java.util
      Map)))

(defn invoice-item->map
  [^InvoiceItem invoice-item]
  {:id           (.getId invoice-item)
   :customer     (.getCustomer invoice-item)
   :object       (.getObject invoice-item)
   :amount       (.getAmount invoice-item)
   :currency     (.getCurrency invoice-item)
   :created-at   (.getDate invoice-item)
   :invoice      (.getInvoice invoice-item)
   :subscription (.getSubscription invoice-item)})

(defn create
  ([^Map params api-key]
   (invoice-item->map
     (InvoiceItem/create
       ^Map (transform-params params)
       (-> (RequestOptions/builder)
           (.setApiKey api-key)
           .build))))
  ([api-key]
   (create {} api-key)))

(defn retrieve
  [id api-key]
  (invoice-item->map
    (InvoiceItem/retrieve
      id
      (-> (RequestOptions/builder)
          (.setApiKey api-key)
          .build))))
