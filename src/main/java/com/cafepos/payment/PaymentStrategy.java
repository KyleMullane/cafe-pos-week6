package com.cafepos.payment;
import com.cafepos.checkout.TaxPolicy;
import com.cafepos.common.Money;
import com.cafepos.order.Order;

public interface PaymentStrategy {
    void process(Order order, TaxPolicy taxPolicy);

    void process(Money money);

    String process(String s, int i, String card, String loyal5, boolean b);
}