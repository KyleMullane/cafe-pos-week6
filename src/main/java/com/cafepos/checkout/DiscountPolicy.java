package com.cafepos.checkout;

import com.cafepos.common.Money;

public interface DiscountPolicy {

    Money discountOf(Money subtotal);
    static DiscountPolicy fromCode(String discountCode) {
        if (discountCode == null || discountCode.equalsIgnoreCase("NONE")) {
            return new NoDiscount();
        }
        if (discountCode.equalsIgnoreCase("LOYAL5")) {
            return new LoyaltyPercentDiscount(5);
        }
        if (discountCode.equalsIgnoreCase("COUPON1")) {
            return new FixedCouponDiscount(Money.of(1.0));
        }
        return new NoDiscount();
    }
}
