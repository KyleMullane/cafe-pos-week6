package com.cafepos.payment;
import com.cafepos.checkout.PaymentStrategy;
import com.cafepos.common.Money;

public class WalletPayment implements PaymentStrategy {
    @Override
    public void process(Money total) {
        System.out.println("[Wallet] Customer paid " + total + " EUR via wallet user-wallet-789");
    }
}