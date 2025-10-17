package com.cafepos.decorator;

import com.cafepos.catalog.Product;
import com.cafepos.common.Money;

public abstract class ProductDecorator implements Product, Priced {
    protected final Product base;
    protected ProductDecorator(Product base) { if (base == null) throw new IllegalArgumentException("base required"); this.base = base; }
    @Override public String id() { return base.id(); }
    @Override public Money basePrice() { return base.basePrice(); }
}
