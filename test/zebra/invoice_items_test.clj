(ns zebra.invoice-items-test
  (:require
    [clojure.test :refer [deftest is testing]]
    [zebra.customers :as customers]
    [zebra.helpers.constants :refer [api-key]]
    [zebra.invoice-items :as invoice-items]
    [zebra.prices :as prices]
    [zebra.products :as products]))

(deftest create-invoice-item
  (let [customer (customers/create api-key)
        product (products/create {:name (str "test_product_" (random-uuid))} api-key)
        product-id (:id product)
        amount 999
        currency "gbp"
        price (prices/create {:unit_amount amount
                              :currency    currency
                              :product     product-id}
                             api-key)
        invoice-item (invoice-items/create
                       {:customer (:id customer)
                        :price    (:id price)}
                       api-key)]

    (testing "should be a valid invoice item"
      (is (some? (:id invoice-item)))
      (testing "with a valid customer"
        (is (= (:id customer) (:customer invoice-item)))
        (is (= amount (:amount invoice-item)))
        (is (= currency (:currency invoice-item)))
        (is (some? (:created-at invoice-item)))))

    (testing "can then retrieve the invoice item"
      (let [retrieved-invoice-item (invoice-items/retrieve (:id invoice-item) api-key)]
        (testing "should still be a valid invoice item"
          (is (some? (:id retrieved-invoice-item)))
          (testing "with a valid customer"
            (is (= (:id customer) (:customer retrieved-invoice-item)))))))))
