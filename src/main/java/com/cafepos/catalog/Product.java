package com.cafepos.catalog;

import com.cafepos.common.Money;

public interface Product {
    String id();
    String name();
    Money basePrice();
}
