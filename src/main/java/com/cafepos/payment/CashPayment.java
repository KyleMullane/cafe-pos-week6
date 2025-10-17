package com.cafepos.payment;
import com.cafepos.common.Money;

public class CashPayment implements com.cafepos.checkout.PaymentStrategy {
    @Override
    public void process(Money total) {
        System.out.println("[Cash] Customer paid " + total + " EUR");
    }
}