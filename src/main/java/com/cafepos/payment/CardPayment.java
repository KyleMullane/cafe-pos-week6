package com.cafepos.payment;
import com.cafepos.checkout.PaymentStrategy;
import com.cafepos.common.Money;

public class CardPayment implements PaymentStrategy {
    @Override
    public void process(Money total) {
        System.out.println("[Card] Customer paid " + total + " EUR with card ****1234");
    }
}