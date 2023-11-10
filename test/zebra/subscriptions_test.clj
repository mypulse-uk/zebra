(ns zebra.subscriptions-test
  (:require
    [clojure.string :as str]
    [clojure.test :refer :all]
    [zebra.customers :as customers]
    [zebra.helpers.constants :refer [api-key]]
    [zebra.payment-methods :as payment-methods]
    [zebra.prices :as prices]
    [zebra.products :as products]
    [zebra.subscriptions :as subscriptions]))

(deftest create-subscription
  (let [customer (customers/create api-key)
        customer-id (:id customer)
        key "some-field"
        value "some value"
        payment-method (payment-methods/create {:type "card"
                                                :card {:number    "4242424242424242"
                                                       :exp_month "7"
                                                       :exp_year  "2026"
                                                       :cvc       "314"}} api-key)
        _attached-payment-method (customers/attach-payment-method customer-id (:id payment-method) api-key)
        product (products/create {:name (str "test_product_" (random-uuid))} api-key)
        product-id (:id product)
        amount 999
        currency "gbp"
        interval "day"
        price (prices/create {:unit_amount amount
                              :currency    currency
                              :recurring   {:interval interval}
                              :product     product-id
                              :metadata    {key value}} api-key)
        price-id (:id price)
        subscription (subscriptions/create {:customer               customer-id
                                            :default_payment_method (:id payment-method)
                                            :payment_behavior       "default_incomplete"
                                            :items                  [{:price price-id}]
                                            :expand                 ["latest_invoice.payment_intent"]
                                            :metadata               {key value}} api-key)]

    (testing "should be a valid subscription"
      (is (some? (:id subscription)))
      (is (= customer-id (:customer subscription)))
      (is (nil? (:cancelled-at subscription)))
      (is (some? (:created-at subscription)))
      (is (some? (:current-period-start subscription)))
      (is (some? (:current-period-end subscription)))
      (is (= "incomplete" (:status subscription)))
      (is (= value (get-in subscription [:metadata key])))

      (testing "should include latest-invoice"
        (let [invoice (:latest-invoice subscription)]
          (is (some? invoice))
          (is (str/starts-with? (:id invoice) "in_"))
          (is (= amount (:amount-due invoice)))
          (is (some? (:amount-paid invoice)))
          (is (some? (:attempt-count invoice)))
          (is (some? (:billing-reason invoice)))
          (is (nil? (:charge invoice)))
          (is (some? (:created-at invoice)))))

      (testing "latest-invoice should include payment-intent"
        (let [payment-intent (get-in subscription [:latest-invoice :payment-intent])]
          (is (str/starts-with? (:id payment-intent) "pi_"))
          (is (= (:object payment-intent) "payment_intent"))
          (is (= "requires_confirmation" (:status payment-intent)))
          (is (= "automatic" (:confirmation_method payment-intent)))
          (is (= (:payment_method_types payment-intent) ["card"]))
          (is (vector? (:payment_method_types payment-intent)))
          (is (= amount (:amount payment-intent)))
          (is (= currency (:currency payment-intent)))
          (is (= (:id payment-method) (:payment_method payment-intent))))))))

(deftest retrieve-subscription
  (let [customer (customers/create api-key)
        customer-id (:id customer)
        key "some-field"
        value "some value"
        payment-method (payment-methods/create {:type "card"
                                                :card {:number    "4242424242424242"
                                                       :exp_month "7"
                                                       :exp_year  "2026"
                                                       :cvc       "314"}} api-key)
        _attached-payment-method (customers/attach-payment-method customer-id (:id payment-method) api-key)
        product (products/create {:name (str "test_product_" (random-uuid))} api-key)
        product-id (:id product)
        amount 999
        currency "gbp"
        interval "day"
        price (prices/create {:unit_amount amount
                              :currency    currency
                              :recurring   {:interval interval}
                              :product     product-id
                              :metadata    {key value}} api-key)
        price-id (:id price)
        created-subscription (subscriptions/create {:customer               customer-id
                                                    :default_payment_method (:id payment-method)
                                                    :payment_behavior       "default_incomplete"
                                                    :items                  [{:price price-id}]
                                                    :expand                 ["latest_invoice.payment_intent"]
                                                    :metadata               {key value}} api-key)
        subscription (subscriptions/retrieve (:id created-subscription) api-key)]

    (testing "should retrieve subscription"
      (is (= (:id created-subscription) (:id subscription))))))

(deftest retrieve-subscription-with-params
  (let [customer (customers/create api-key)
        customer-id (:id customer)
        key "some-field"
        value "some value"
        payment-method (payment-methods/create {:type "card"
                                                :card {:number    "4242424242424242"
                                                       :exp_month "7"
                                                       :exp_year  "2026"
                                                       :cvc       "314"}} api-key)
        _attached-payment-method (customers/attach-payment-method customer-id (:id payment-method) api-key)
        product (products/create {:name (str "test_product_" (random-uuid))} api-key)
        product-id (:id product)
        amount 999
        currency "gbp"
        interval "day"
        price (prices/create {:unit_amount amount
                              :currency    currency
                              :recurring   {:interval interval}
                              :product     product-id
                              :metadata    {key value}} api-key)
        price-id (:id price)
        expand-param ["latest_invoice.payment_intent"]
        created-subscription (subscriptions/create {:customer               customer-id
                                                    :default_payment_method (:id payment-method)
                                                    :payment_behavior       "default_incomplete"
                                                    :items                  [{:price price-id}]
                                                    :expand                 expand-param
                                                    :metadata               {key value}} api-key)
        subscription (subscriptions/retrieve (:id created-subscription) {:expand expand-param} api-key)]

    (testing "should retrieve subscription"
      (is (= (:id created-subscription) (:id subscription))))

    (testing "should include latest-invoice"
      (is (= (get-in created-subscription [:latest-invoice :id])
             (get-in subscription [:latest-invoice :id]))))))

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
