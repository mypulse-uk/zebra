(ns zebra.invoices
  (:require
    [zebra.utils :refer [transform-params]])
  (:import
    (com.stripe.model
      Invoice)
    (com.stripe.net
      RequestOptions)
    (java.util
      Map)))

(defn invoice->map
  [^Invoice invoice]
  {:id       (.getId invoice)
   :customer (.getCustomer invoice)
   :metadata (.getMetadata invoice)
   :payment_intent  (.getPaymentIntent invoice)
   :total (.getTotal invoice)})

(defn create
  ([^Map params api-key]
   (invoice->map
     (Invoice/create
       ^Map (transform-params params)
       (-> (RequestOptions/builder)
           (.setApiKey api-key)
           .build))))
  ([api-key]
   (create {} api-key)))

(defn retrieve
  [id api-key]
  (invoice->map
    (Invoice/retrieve
      id
      (-> (RequestOptions/builder)
          (.setApiKey api-key)
          .build))))

(defn finalise
  [id api-key]
  (let [opts (-> (RequestOptions/builder) (.setApiKey api-key) .build)
        invoice (Invoice/retrieve id opts)
        finalised-invoice (.finalizeInvoice invoice opts)]
    (invoice->map
      finalised-invoice)))