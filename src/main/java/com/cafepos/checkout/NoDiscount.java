package com.cafepos.checkout;
import com.cafepos.common.Money;
public final class NoDiscount implements DiscountPolicy {
    @Override public Money discountOf(Money subtotal) { return
            Money.zero(); }
}