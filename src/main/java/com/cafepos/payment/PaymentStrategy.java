package com.cafepos.payment;
import com.cafepos.common.Money;

public interface PaymentStrategy {
    void process(Money total);
}