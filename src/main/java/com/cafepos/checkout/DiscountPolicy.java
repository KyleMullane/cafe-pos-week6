package com.cafepos.checkout;
import com.cafepos.common.Money;
public interface DiscountPolicy {
    Money discountOf(Money subtotal);
}