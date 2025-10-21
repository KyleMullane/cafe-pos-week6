package com.cafepos.checkout;
import com.cafepos.common.Money;
import com.cafepos.order.Order;
import com.cafepos.payment.PaymentStrategy;

public class UnknownPayment implements PaymentStrategy {
    public void process(Money total) {
        System.out.println("[UnknownPayment] " + total);
    }

    @Override
    public String process(String s, int i, String card, String loyal5, boolean b) {
        return "";
    }

    @Override
    public void process(Order order, TaxPolicy taxPolicy) {

    }
}