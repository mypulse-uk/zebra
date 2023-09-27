(ns zebra.subscriptions-test
  (:require
    [clojure.test :refer :all]
    [zebra.customers :as customers]
    [zebra.payment-methods :as payment-methods]
    [zebra.prices :as prices]
    [zebra.products :as products]
    [zebra.subscriptions :as subscriptions]
    [zebra.helpers.constants :refer [api-key]]))

(deftest create-subscription
  (let [customer (customers/create api-key)
        customer-id (:id customer)
        payment-method (payment-methods/create {:type "card"
                                                :card {:number    "4242424242424242"
                                                       :exp_month "7"
                                                       :exp_year  "2026"
                                                       :cvc       "314"}} api-key)
        _attached-payment-method (customers/attach-payment-method customer-id (:id payment-method) api-key)
        product (products/create {:name (str "test_product_" (random-uuid))} api-key)
        product-id (:id product)
        price (prices/create {:unit_amount 999
                              :currency    "gbp"
                              :recurring   {:interval "day"}
                              :product     product-id} api-key)
        price-id (:id price)
        key "some-field"
        value "some value"
        subscription (subscriptions/create {:customer               customer-id
                                            :default_payment_method (:id payment-method)
                                            :items                  [{:price price-id}]
                                            :metadata               {key value}} api-key)]
    (testing "should be a valid subscription"
      (is (some? (:id subscription)))
      (is (= customer-id (:customer subscription)))
      (is (nil? (:cancelled-at subscription)))
      (is (some? (:created-at subscription)))
      (is (some? (:current-period-start subscription)))
      (is (some? (:current-period-end subscription)))
      (is (= "active" (:status subscription)))
      (is (= value (get-in subscription [:metadata key]))))))

(deftest list-subscriptions
  (let [customer (customers/create api-key)
        customer-id (:id customer)
        payment-method (payment-methods/create {:type "card"
                                                :card {:number    "4242424242424242"
                                                       :exp_month "7"
                                                       :exp_year  "2026"
                                                       :cvc       "314"}} api-key)
        _attached-payment-method (customers/attach-payment-method customer-id (:id payment-method) api-key)
        product (products/create {:name (str "test_product_" (random-uuid))} api-key)
        product-id (:id product)
        price (prices/create {:unit_amount 999
                              :currency    "gbp"
                              :recurring   {:interval "day"}
                              :product     product-id} api-key)
        price-id (:id price)
        subscription (subscriptions/create {:customer               customer-id
                                            :default_payment_method (:id payment-method)
                                            :items                  [{:price price-id}]} api-key)
        retrieved-subscriptions (subscriptions/list api-key)]
    (testing "should retrieve a list of subscriptions"
      (is (some? retrieved-subscriptions))
      (is (some? (:subscriptions retrieved-subscriptions)))
      (is (some #(= (:id subscription) (:id %)) (:subscriptions retrieved-subscriptions))))))
