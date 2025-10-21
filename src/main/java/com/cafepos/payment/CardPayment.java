package com.cafepos.payment;
import com.cafepos.checkout.TaxPolicy;
import com.cafepos.common.Money;
import com.cafepos.order.Order;

public class CardPayment implements PaymentStrategy {
    @Override
    public void process(Order order, TaxPolicy taxPolicy) {

    }

    @Override
    public void process(Money total) {
        System.out.println("[Card] Customer paid " + total + " EUR with card ****1234");
    }

    @Override
    public String process(String s, int i, String card, String loyal5, boolean b) {
        return "";
    }
}