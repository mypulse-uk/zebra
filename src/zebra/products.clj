(ns zebra.products
  (:refer-clojure :exclude [list update])
  (:import
    (com.stripe.model
      Product)
    (com.stripe.net
      RequestOptions)
    (java.util
      Map)))

(defn product->map
  [^Product product]
  {:id       (.getId product)
   :name     (.getName product)
   :metadata (.getMetadata product)})

(defn create
  [^Map params api-key]
  (product->map
    (Product/create params
                    (-> (RequestOptions/builder) (.setApiKey api-key) .build))))

(defn retrieve
  [id api-key]
  (product->map
    (Product/retrieve id
                      (-> (RequestOptions/builder) (.setApiKey api-key) .build))))
