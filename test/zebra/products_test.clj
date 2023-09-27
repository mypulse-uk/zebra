(ns zebra.products-test
  (:require
    [clojure.test :refer :all]
    [zebra.products :as products]
    [zebra.helpers.constants :refer [api-key]]))

(deftest create-product
  (let [product-name (str "test_product_" (random-uuid))
        product (products/create {"name" product-name} api-key)]
    (testing "should be a valid product"
      (is (some? (:id product)))
      (is (= product-name (:name product))))))

(deftest create-product-with-metadata
  (let [key "some-field"
        value "some value"
        product-name (str "test_product_" (random-uuid))
        product (products/create {"name"     product-name
                                  "metadata" {key value}} api-key)]
    (testing "should be a valid product"
      (is (some? (:id product)))
      (is (= value (get-in product [:metadata key]))))))

(deftest retrieve-product
  (let [product-name (str "test_product_" (random-uuid))
        product (products/create {"name" product-name} api-key)
        retrieved-product (products/retrieve (:id product) api-key)]
    (testing "should retrieve a created product"
      (is (some? (:id retrieved-product))))))
