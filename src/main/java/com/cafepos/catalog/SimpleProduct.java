package com.cafepos.catalog;

import com.cafepos.common.Money;
import com.cafepos.decorator.Priced;

public final class SimpleProduct implements Product, Priced {
    private final String id;
    private final String name;
    private final Money basePrice;

    public SimpleProduct(String id, String name, Money basePrice) {
        if (id == null || id.isBlank()) throw new IllegalArgumentException("id required");
        if (name == null || name.isBlank()) throw new IllegalArgumentException("name required");
        if (basePrice == null || basePrice.asBigDecimal().signum() < 0)
            throw new IllegalArgumentException("basePrice >= 0 required");
        this.id = id; this.name = name; this.basePrice = basePrice;
    }

    @Override public String id() { return id; }
    @Override public String name() { return name; }
    @Override public Money basePrice() { return basePrice; }
    @Override public Money price() { return basePrice; }
}
