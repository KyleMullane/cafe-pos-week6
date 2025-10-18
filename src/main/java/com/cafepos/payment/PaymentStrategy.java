package com.cafepos.payment;
import com.cafepos.checkout.TaxPolicy;
import com.cafepos.order.Order;

public interface PaymentStrategy {
    void process(Order order, TaxPolicy taxPolicy);
}