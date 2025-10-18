package com.cafepos.order;

import com.cafepos.common.Money;
import java.math.BigDecimal;
import java.util.*;

public final class Order {
    private long id = 0;
    private final List<LineItem> items = new ArrayList<>();
    public Order(long id) { this.id = id; }

    public Order(Money subtotal, Money discount, Money tax, Money total) {
        this.id = id;
    }


    public long id() { return id; }
    public List<LineItem> items() { return List.copyOf(items); }
    public void addItem(LineItem li) { items.add(li); }
    public Money subtotal() { return items.stream().map(LineItem::lineTotal).reduce(Money.zero(), Money::add); }
    public Money taxAtPercent(int percent) {
        BigDecimal tax = subtotal().asBigDecimal().multiply(BigDecimal.valueOf(percent)).divide(BigDecimal.valueOf(100));
        return Money.of(tax);
    }
    public Money totalWithTax(int percent) { return subtotal().add(taxAtPercent(percent)); }
}
