(ns zebra.prices-test
  (:require
    [clojure.test :refer :all]
    [zebra.helpers.constants :refer [api-key]]
    [zebra.prices :as prices]
    [zebra.products :as products]))

(deftest create-price
  (let [unit-amount 999
        currency "gbp"
        product (products/create {:name (str "test_product_" (random-uuid))} api-key)
        product-id (:id product)
        price (prices/create {:unit_amount unit-amount
                              :currency    currency
                              :product     product-id} api-key)]
    (testing "should be a valid price"
      (is (some? (:id price)))
      (is (= unit-amount (:unit-amount price)))
      (is (= currency (:currency price)))
      (is (= product-id (:product price))))))

(deftest create-price-with-metadata
  (let [key "some-field"
        value "some value"
        unit-amount 999
        currency "gbp"
        product (products/create {:name (str "test_product_" (random-uuid))} api-key)
        product-id (:id product)
        price (prices/create {:unit_amount unit-amount
                              :currency    currency
                              :product     product-id
                              :metadata    {key value}} api-key)]
    (testing "should be a valid price"
      (is (some? (:id price)))
      (is (= value (get-in price [:metadata key]))))))

(deftest retrieve-price
  (let [unit-amount 999
        currency "gbp"
        product (products/create {:name (str "test_product_" (random-uuid))} api-key)
        product-id (:id product)
        price (prices/create {:unit_amount unit-amount
                              :currency    currency
                              :product     product-id} api-key)
        retrieved-price (prices/retrieve (:id price) api-key)]
    (testing "should retrieve a created price"
      (is (some? (:id retrieved-price))))))

(deftest search-prices-with-query-for-matching-price
  (let [key "some-field"
        value "some value"
        unit-amount 999
        currency "gbp"
        product (products/create {:name (str "test_product_" (random-uuid))} api-key)
        product-id (:id product)
        _price-1 (prices/create {:unit_amount unit-amount
                                 :currency    currency
                                 :product     product-id
                                 :metadata    {key value}} api-key)
        _price-2 (prices/create {:unit_amount unit-amount
                                 :currency    currency
                                 :product     product-id} api-key)
        result (prices/search {:query (str "metadata[\"" key "\"]:\"" value "\"")
                               :limit 1} api-key)]

    (testing "should retrieve matching price"
      (is (some? (:prices result))))))

(deftest search-prices-with-query-for-non-matching-price
  (let [key "some-field"
        result (prices/search {:query (str "metadata[\"" key "\"]:\"some-non-matching-value\"")
                               :limit 1} api-key)]

    (testing "should retrieve matching price"
      (is (empty? (:prices result))))))
