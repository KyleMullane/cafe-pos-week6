package com.cafepos.payment;
import com.cafepos.checkout.TaxPolicy;
import com.cafepos.common.Money;
import com.cafepos.order.Order;

public class WalletPayment implements PaymentStrategy {
    public WalletPayment() {
    }

    @Override
    public void process(Money total) {
        System.out.println("[Wallet] Customer paid " + total + " EUR via wallet user-wallet-789");
    }

    @Override
    public void process(Order order, TaxPolicy taxPolicy) {

    }
}