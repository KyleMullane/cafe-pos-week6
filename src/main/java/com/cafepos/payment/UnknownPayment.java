package com.cafepos.checkout;
import com.cafepos.common.Money;
import com.cafepos.payment.PaymentStrategy;

public class UnknownPayment implements PaymentStrategy {
    @Override
    public void process(Money total) {
        System.out.println("[UnknownPayment] " + total);
    }
}